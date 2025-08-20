package com.itachi1706.busarrivalsg.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.itachi1706.busarrivalsg.R
import com.itachi1706.busarrivalsg.database.BusStopsDb
import com.itachi1706.busarrivalsg.databinding.FragmentBusStopsNearbyBinding
import com.itachi1706.busarrivalsg.objects.gson.ltasg.BusStopJSON
import com.itachi1706.busarrivalsg.recyclerviews.BusStopRecyclerAdapter
import com.itachi1706.busarrivalsg.tasks.PopulateListCurrentLocationTask
import com.itachi1706.busarrivalsg.util.BusesUtil
import com.itachi1706.busarrivalsg.util.OnMapViewReadyListener
import com.itachi1706.helperlib.concurrent.Constants
import com.itachi1706.helperlib.helpers.LogHelper.d
import com.itachi1706.helperlib.helpers.LogHelper.e
import com.itachi1706.helperlib.helpers.LogHelper.w

class BusStopsNearbyFragment : Fragment(), OnMapViewReadyListener.OnGlobalMapReadyListener,
    GoogleMap.OnInfoWindowClickListener {

        companion object {
            private const val TAG = "BSNearbyF"

            const val RECEIVE_LOCATION_EVENT = "ReceiveLocationEvent"
            const val RECEIVE_NEARBY_STOPS_EVENT = "ReceiveNearbyEvent"
        }

    private var binding: FragmentBusStopsNearbyBinding? = null

    private var adapter: BusStopRecyclerAdapter? = null
    private var mMap: GoogleMap? = null
    private var locationManager: LocationManager? = null
    private var db: BusStopsDb? = null

    private var isAnimating = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBusStopsNearbyBinding.inflate(inflater, container, false)
        val view = binding?.root

        if (activity == null) {
            e(TAG, "No activity found, cannot proceed with BusStopsNearbyFragment")
            return view
        }

        binding?.rvNearestBusStops?.setHasFixedSize(true)
        val llm = LinearLayoutManager(context)
        llm.orientation = RecyclerView.VERTICAL
        binding?.rvNearestBusStops?.layoutManager = llm
        binding?.rvNearestBusStops?.itemAnimator = DefaultItemAnimator()

        adapter = BusStopRecyclerAdapter(mutableListOf<BusStopJSON>())
        binding?.rvNearestBusStops?.adapter = adapter

        // Populate with empty view
        db = BusStopsDb(requireContext())
        if (db == null) {
            e(TAG, "Database is null, cannot proceed with BusStopsNearbyFragment")
            return view
        }

        val results = db!!.getAllBusStops()
        adapter?.updateAdapter(results)
        adapter?.notifyItemRangeChanged(0, results.size)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.window?.decorView?.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.mapView?.onCreate(savedInstanceState)
        binding?.mapView?.let { OnMapViewReadyListener(it, this) }
    }

    override fun onResume() {
        super.onResume()
        binding?.mapView?.onResume()
        if (context != null) {
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(receiver, IntentFilter(RECEIVE_LOCATION_EVENT))
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(nearbyReceiver, IntentFilter(RECEIVE_NEARBY_STOPS_EVENT))
        }
    }

    override fun onPause() {
        super.onPause()
        binding?.mapView?.onPause()
        if (context != null) {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver)
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(nearbyReceiver)
        }
    }

    private var nearbyTask: PopulateListCurrentLocationTask? = null
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: android.content.Context, intent: android.content.Intent) {
            val location = Location("")
            location.latitude = intent.getDoubleExtra("lat", 0.0)
            location.longitude = intent.getDoubleExtra("lng", 0.0)
            if (db == null) {
                db = BusStopsDb(requireContext())
            }

            if (nearbyTask == null || nearbyTask?.status == Constants.Status.FINISHED) {
                nearbyTask = PopulateListCurrentLocationTask(requireActivity(), db!!, adapter!!)
                nearbyTask?.execute(location)
            }
        }
    }

    private var markerMap: HashMap<Marker, BusStopJSON>? = null
    private val nearbyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: android.content.Context, intent: android.content.Intent) {
            if (mMap == null) return // Don't do anything as no map has been initialized
            // If invoked means GPS granted so ignore check
            if (locationManager == null) checkGpsForCurrentLocation()

            mMap?.clear()
            if (markerMap == null) markerMap = HashMap()
            markerMap?.clear()

            val data = intent.getStringExtra("data")
            val gson = Gson()
            val listType = object : TypeToken<ArrayList<BusStopJSON?>?>() {}.type
            val stops = gson.fromJson<ArrayList<BusStopJSON>>(data, listType)

            for (stop in stops) {
                val serviceString = stop.services
                if (serviceString.isEmpty()) {
                    w(TAG, "No services found for bus stop ${stop.busStopCode}, skipping marker creation")
                    continue
                }

                val svcRaw = serviceString.split(",")
                val services = StringBuilder()
                for (svc in svcRaw) {
                    services.append(svc.split(":")[0]).append(", ")
                }
                val markerOption = MarkerOptions().position(LatLng(stop.latitude, stop.longitude))
                    .title("${stop.description} (${stop.roadName})")
                    .snippet("Bus Svcs: ${services.toString().replace(", $", "")}")
                    .icon(BusesUtil.vectorToBitmapDescriptor(R.drawable.red_circle, context))
                mMap?.addMarker(markerOption)?.let { markerMap?.put(it, stop) }
            }

            mMap?.setOnMapLoadedCallback { zoomToLocation() }
        }
    }

    private fun checkGpsForCurrentLocation() {
        if (context == null) return
        val rc = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
        if (rc == PackageManager.PERMISSION_GRANTED) {
            mMap?.isMyLocationEnabled = true
            locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        d(TAG, "Google Map Ready")
        mMap = googleMap
        mMap?.isTrafficEnabled = true
        checkGpsForCurrentLocation()
        mMap?.setOnInfoWindowClickListener(this)
        val settings = mMap?.uiSettings
        settings?.isZoomControlsEnabled = true
        settings?.isMapToolbarEnabled = true

        mMap?.setOnMapLoadedCallback(this::zoomToLocation)
    }

    @SuppressLint("MissingPermission")
    private fun zoomToLocation() {
        if (locationManager != null && !isAnimating) {
            // Assume location permission granted for it to be initialized to zoom to current location
            isAnimating = true
            val myLoc = locationManager?.getLastKnownLocation(locationManager?.getBestProvider(
                Criteria(), false)!!)
            if (myLoc == null) return
            val myLatLng = LatLng(myLoc.latitude, myLoc.longitude)
            d(TAG, "animateCamera:onStart: $myLatLng")
            mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 17f), object: GoogleMap.CancelableCallback {
                override fun onFinish() {
                    d(TAG, "animateCamera:onFinish")
                    isAnimating = false
                }

                override fun onCancel() {
                    d(TAG, "animateCamera:onCancel")
                    isAnimating = false
                }
            })
        }
    }

    override fun onInfoWindowClick(marker: Marker) {
        d(TAG, "Marker Info Clicked (${marker.title})")
        val stop = markerMap?.get(marker)
        if (stop == null) return
        adapter?.handleClick(context, stop)
    }
}
package com.itachi1706.busarrivalsg.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.itachi1706.busarrivalsg.R
import com.itachi1706.busarrivalsg.databinding.DialogBusLocationMapBinding
import com.itachi1706.busarrivalsg.objects.CommonEnums
import com.itachi1706.busarrivalsg.util.BusesUtil
import com.itachi1706.busarrivalsg.util.OnMapViewReadyListener
import com.itachi1706.busarrivalsg.util.StaticVariables
import com.itachi1706.helperlib.helpers.LogHelper

class BusLocationMapsDialogFragment : DialogFragment(),
    OnMapViewReadyListener.OnGlobalMapReadyListener {

    private var mMap: GoogleMap? = null
    private var busLatitude = 0.0
    private var busLongitude = 0.0

    private var lat1 = 0.0
    private var lng1 = 0.0
    private var lat2 = 0.0
    private var lng2 = 0.0
    private var lat3 = 0.0
    private var lng3 = 0.0
    private var arr1 = "Unknown"
    private var arr2 = "Unknown"
    private var arr3 = "Unknown"
    private var type1 = CommonEnums.UNKNOWN
    private var type2 = CommonEnums.UNKNOWN
    private var type3 = CommonEnums.UNKNOWN
    private var curTime: String? = null
    private var state = 0

    companion object {
        private const val TAG = "BusMapFrag"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NO_TITLE, R.style.AppTheme_AlertDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DialogBusLocationMapBinding.inflate(inflater, container, false)

        binding.closeBtn.setOnClickListener { dismiss() }
        val mapFragment = SupportMapFragment()

        dialog?.setTitle("")
        val transaction = childFragmentManager.beginTransaction()
        transaction.add(binding.mapView.id, mapFragment).commit()

        arguments?.let {
            busLatitude = it.getDouble("buslat", 0.0)
            busLongitude = it.getDouble("buslng", 0.0)

            val bc = it.getString("busCode", "Unknown")
            val bsn = it.getString("busSvcNo", "Unknown")

            lat1 = it.getDouble("lat1", 0.0)
            lng1 = it.getDouble("lng1", 0.0)
            lat2 = it.getDouble("lat2", 0.0)
            lng2 = it.getDouble("lng2", 0.0)
            lat3 = it.getDouble("lat3", 0.0)
            lng3 = it.getDouble("lng3", 0.0)
            arr1 = it.getString("arr1", "Unknown")
            arr2 = it.getString("arr2", "Unknown")
            arr3 = it.getString("arr3", "Unknown")
            type1 = it.getInt("type1", CommonEnums.UNKNOWN)
            type2 = it.getInt("type2", CommonEnums.UNKNOWN)
            type3 = it.getInt("type3", CommonEnums.UNKNOWN)
            state = it.getInt("state", StaticVariables.CUR)
            curTime = it.getString("sTime", null)

            val mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.ITEM_ID, "Service No: $bsn | Stop Code: $bc")
                putString(FirebaseAnalytics.Param.CONTENT_TYPE, "mapDialogLaunched")
            }
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
        }

        OnMapViewReadyListener(mapFragment, this)
        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
        mMap?.isTrafficEnabled = true

        // Formally form layout
        mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap?.uiSettings?.apply {
            isCompassEnabled = true
            isRotateGesturesEnabled = true
            isScrollGesturesEnabled = true
            isTiltGesturesEnabled = true
            isZoomControlsEnabled = true
            isZoomGesturesEnabled = true
        }

        checkIfYouHaveGpsPermissions()

        // Add marker to bus location and move camera and add bus locations
        val busStopLocation = LatLng(busLatitude, busLongitude)
        val m1 = addBusLocation(lat1, lng1, 1, arr1, type1)
        val m2 = addBusLocation(lat2, lng2, 2, arr2, type2)
        val m3 = addBusLocation(lat3, lng3, 3, arr3, type3)
        val b = LatLngBounds.Builder()

        // Process bus locations
        m1?.let { b.include(it.position) }
        m2?.let {
            if (state == StaticVariables.NEXT || state == StaticVariables.SUB)
                b.include(it.position)
        }
        m3?.let {
            if (state == StaticVariables.SUB)
                b.include(it.position)
        }

        val cur = BusesUtil.getCurrentMarker(m1, m2, m3, state)
        val stop = mMap?.addMarker(
            MarkerOptions().position(busStopLocation)
                .title(getString(R.string.maps_marker_bus_stop_title))
                .snippet(getString(R.string.maps_marker_bus_stop_snippet))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pegman))
        )
        stop?.position?.let { b.include(it) }
        val boundary = b.build()
        mMap?.setOnMapLoadedCallback {
            mMap?.animateCamera(
                CameraUpdateFactory.newLatLngBounds(boundary, 100),
                500,
                object : GoogleMap.CancelableCallback {
                    override fun onFinish() {
                        LogHelper.i(TAG, "Map moving animation finished")
                        cur?.showInfoWindow()
                    }

                    override fun onCancel() {
                        LogHelper.i(TAG, "Map moving animation cancelled")
                    }
                })
        }

    }

    private fun addBusLocation(lat: Double, lng: Double, no: Int, arr: String, type: Int): Marker? {
        return if (StaticVariables.checkBusLocationValid(lat, lng)) {
            mMap?.addMarker(
                MarkerOptions().position(LatLng(lat, lng)).title("Location of Bus $no")
                    .snippet("ETA: ${processArrival(arr)} (${processType(type)})")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_stop))
            )
        } else null
    }

    private fun processArrival(estString: String): String {
        val sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val est = StaticVariables.parseLTAEstimateArrival(
            estString,
            StaticVariables.useServerTime(sp),
            curTime
        )
        return when {
            est == -9999L -> "="
            est <= 0 -> "Arriving"
            else -> "$est mins"
        }
    }

    private fun processType(type: Int): String {
        return when (type) {
            CommonEnums.BUS_BENDY -> "Bendy Bus"
            CommonEnums.BUS_DOUBLE_DECK -> "Double Decker Bus"
            CommonEnums.BUS_SINGLE_DECK -> "Normal Bus"
            else -> "Unknown Bus Type"
        }
    }

    private fun checkIfYouHaveGpsPermissions() {
        val rc = ActivityCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (rc == PackageManager.PERMISSION_GRANTED) {
            mMap?.isMyLocationEnabled = true
        } else {
            requestGpsPermission()
        }
    }

    private fun requestGpsPermission() {
        LogHelper.w(TAG, "GPS Permission not granted. Requestion permission")
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            requestGps.launch(permissions)
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_title_request_permission_gps)
                .setMessage(R.string.dialog_message_request_permission_gps_view_map_rationale)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    requestGps.launch(permissions)
                }.show()
        }
    }

    @SuppressLint("MissingPermission")
    private val requestGps =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            {
                val hasPerm = StaticVariables.checkIfCoarseLocationGranted(result)

                if (hasPerm) {
                    LogHelper.d(TAG, "Location permission granted: Enabling my location")
                    mMap?.isMyLocationEnabled = true // Checked already
                } else {
                    LogHelper.e(TAG, "Location permission not granted")
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.dialog_title_permission_denied)
                        .setMessage(R.string.dialog_message_no_permission_gps)
                        .setPositiveButton(android.R.string.ok, null)
                        .setNeutralButton(R.string.dialog_action_neutral_app_settings) { _, _ ->
                            val permIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri = "package:${requireActivity().packageName}".toUri()
                            permIntent.data = uri
                            startActivity(permIntent)
                        }.show()
                }
            }
        }
}
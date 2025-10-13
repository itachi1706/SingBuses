package com.itachi1706.busarrivalsg

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.ArrayMap
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.scale
import androidx.core.graphics.toColorInt
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.itachi1706.busarrivalsg.databinding.ActivityNtubusWithSheetBinding
import com.itachi1706.busarrivalsg.objects.CommonEnums
import com.itachi1706.busarrivalsg.objects.json.ltasg.BusArrivalArrayObject
import com.itachi1706.busarrivalsg.objects.json.ltasg.BusArrivalArrayObjectEstimate
import com.itachi1706.busarrivalsg.objects.json.ltasg.BusArrivalMain
import com.itachi1706.busarrivalsg.objects.json.ltasg.BusStopJSON
import com.itachi1706.busarrivalsg.tasks.GetNTUPublicBusTask
import com.itachi1706.busarrivalsg.util.BusesUtil
import com.itachi1706.busarrivalsg.util.OnMapViewReadyListener
import com.itachi1706.busarrivalsg.util.StaticVariables
import com.itachi1706.helperlib.concurrent.Constants
import com.itachi1706.helperlib.helpers.EdgeToEdgeHelper
import com.itachi1706.helperlib.helpers.LogHelper
import com.itachi1706.helperlib.objects.ApiResponse
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.util.Date

class NtuBusActivity : AppCompatActivity(), OnMapViewReadyListener.OnGlobalMapReadyListener, GoogleMap.OnInfoWindowClickListener {

    private var binding: ActivityNtubusWithSheetBinding? = null

    private var mMap: GoogleMap? = null
    private var autoRefreshDelay = -1

    private var mapReady = false

    // Markers
    private val publicBusMarkers = mutableListOf<Marker>()
    private var runningPBus: GetNTUPublicBusTask? = null
    private var refreshHandler: Handler? = null
    private var shouldAutoRefresh = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNtubusWithSheetBinding.inflate(layoutInflater)
        EdgeToEdgeHelper.setEdgeToEdgeWithContentView(binding?.root!!, this)

        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            val home = BusesUtil.vectorToBitmap(R.drawable.ic_ntu_coa, this, null)
            val d = home.scale(
                BusesUtil.pxFromDp(26f, resources).toInt(),
                BusesUtil.pxFromDp(32f, resources).toInt()
            ).toDrawable(resources)
            it.setHomeAsUpIndicator(d)
        }

        // Init map
        binding?.extendLayout?.mapView?.let {
            it.onCreate(savedInstanceState)
            OnMapViewReadyListener(it, this)
            LogHelper.i(TAG, "Creating Map")
        }
    }

    override fun onResume() {
        super.onResume()

        binding?.extendLayout?.mapView?.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(publicBusReceiver,
            IntentFilter(RECEIVE_NTU_PUBLIC_BUS_DATA_EVENT)
        )

        autoRefreshDelay = PreferenceManager.getDefaultSharedPreferences(this)
            .getString("ntushuttlerefrate", "10")?.toInt() ?: 10
        if (autoRefreshDelay < 5) autoRefreshDelay = 5 // Minimum 5 seconds
        shouldAutoRefresh = true
    }

    override fun onPause() {
        super.onPause()

        binding?.extendLayout?.mapView?.onPause()
        shouldAutoRefresh = false
        if (refreshHandler != null) {
            refreshHandler?.removeMessages(REFRESH_TASK)
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(publicBusReceiver)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_ntu_buses, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, MainSettings::class.java))
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            R.id.refresh -> {
                LogHelper.i(TAG, "Manually refreshing bus data at ${
                    StaticVariables.convertDateToString(
                        Date(System.currentTimeMillis())
                    )}")
                getData(true)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        LogHelper.d(TAG, "onMapReady()")
        mMap = googleMap
        mMap?.isTrafficEnabled = true
        checkIfYouHaveGpsPermissionForThis()
        mMap?.setOnInfoWindowClickListener(this)
        val settings = mMap?.uiSettings
        settings?.isZoomControlsEnabled = true
        settings?.isMapToolbarEnabled = false
        mapReady = true

        LogHelper.d(TAG, "Map Created")

        mMap?.setOnMapLoadedCallback { mMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    1.3478184567642855,
                    103.68342014685716
                ), 15.0f)) // Hardcode center of NTU
        }

        refreshHandler = Handler(Looper.getMainLooper())
        getData(false)
    }

    private fun getData(refresh: Boolean) {
        if (!mapReady) return
        if (runningPBus == null || runningPBus?.status == Constants.Status.FINISHED || runningPBus?.isCancelled == true) {
            runningPBus = GetNTUPublicBusTask(this, refresh)
            runningPBus?.execute()
        }

        refreshHandler?.let {
            if (!it.hasMessages(REFRESH_TASK) && shouldAutoRefresh) {
                val ref = Message.obtain(refreshHandler, refreshTask)
                ref.what = REFRESH_TASK
                it.sendMessageDelayed(ref, autoRefreshDelay * 1000L)
            }
        }
    }

    private fun checkIfYouHaveGpsPermissionForThis() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap?.isMyLocationEnabled = true
        } else {
            requestGpsPermission()
        }
    }

    private fun requestGpsPermission() {
        LogHelper.w(TAG, "GPS permission is not granted. Requesting permission")
        val permissions = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            requestGps.launch(permissions)
            return
        }

        MaterialAlertDialogBuilder(this).setTitle(R.string.dialog_title_request_permission_gps)
            .setMessage(R.string.dialog_message_request_permission_gps_view_map_rationale)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                requestGps.launch(permissions)
            }.show()
    }

    @SuppressLint("MissingPermission")
    private val requestGps = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (StaticVariables.checkIfCoarseLocationGranted(it)) {
            LogHelper.d(TAG, "Location permission granted - enabling my location")
            mMap?.isMyLocationEnabled = true
        } else {
            LogHelper.d(TAG, "Permission not granted")
            Toast.makeText(this, "No permission to get current location", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onInfoWindowClick(marker: Marker) {
        if (marker.tag == null) return
        val json = marker.tag
        LogHelper.d(TAG, "Info clicked of ${json?.javaClass?.simpleName}")
        if (json is BusStopJSON) {
            val pBusIntent = Intent(this, BusServicesAtStopRecyclerActivity::class.java)
            pBusIntent.putExtra("stopCode", json.busStopCode)
            pBusIntent.putExtra("stopName", json.description)
            startActivity(pBusIntent)
        }
    }

    private val refreshTask = Runnable {
        LogHelper.i(TAG, "Auto-refreshing bus data at ${
            StaticVariables.convertDateToString(
                Date(
                    System.currentTimeMillis()
                )
            )}")
        getData(true)
    }

    // Parsing and processing data received for public API calls
    private val publicBusReceiver = object: BroadcastReceiver() {
        private val pBusColor = "#ff3333".toColorInt()

        private fun processBusStopData(context: Context, data: String) {
            LogHelper.d(TAG, "Processing Bus Stops Data")
            val json = Json { ignoreUnknownKeys = true }
            val tmpJson = try {
                json.decodeFromString<Array<BusStopJSON>>(data)

            } catch (_ : SerializationException) {
                Toast.makeText(
                    context,
                    "An error occurred parsing public bus stops. Please try again later",
                    Toast.LENGTH_LONG
                ).show()
                null
            }

            if (tmpJson == null) return

            // Convert to something workable and unique
            val busStops = ArrayMap<String, BusStopJSON>()
            for (j in tmpJson) {
                busStops[j.busStopCode] = j
            }
            val stop = BusesUtil.vectorToBitmapDescriptor(R.drawable.ic_circle, context, pBusColor)
            for (entry in busStops.entries) {
                val node = entry.value
                val svcWork = node.services
                val svcs = svcWork.split(",")
                val sb = StringBuilder()
                for (s1 in svcs) {
                    val svcTmp = s1.split(":")
                    sb.append(svcTmp[0]).append(", ")
                }
                var services = sb.toString()
                services = services.replace(Regex(", $"), "")
                val m = mMap?.addMarker(
                    MarkerOptions().position(LatLng(node.latitude, node.longitude))
                    .title("${node.description} (${node.roadName})")
                    .snippet("Bus Svcs: $services")
                    .icon(stop))
                m?.tag = node
                LogHelper.i(TAG, "Generated Public Bus Stops")
            }
        }

        private fun processBusServicesData(context: Context, data: String) {
            val json = Json { ignoreUnknownKeys = true }
            LogHelper.d(TAG, "Processing Bus Services Data")
            val busObjsArr = try {
                val tmp = json.decodeFromString<ApiResponse>(data)
                tmp.getTypedData<Array<BusArrivalMain>>(json)
            } catch (_: SerializationException) {
                Toast.makeText(
                    context,
                    "An error occurred parsing public buses. Please try again later",
                    Toast.LENGTH_LONG
                ).show()
                null
            }

            if (busObjsArr == null) return

            for (busObjs in busObjsArr) {
                if (busObjs.services == null || busObjs.services.size == 0) continue // No action

                val o = busObjs.services[0]
                // Remove markers for this service
                val iter = publicBusMarkers.iterator()
                while (iter.hasNext()) {
                    val m = iter.next()
                    if (m.title == null) continue

                    if (m.title == "${o.serviceNo} (${o.operator})") {
                        m.remove()
                        iter.remove()
                    }
                }

                val e1 = o.nextBus
                addPublicBuses(e1, o)
                val e2 = o.nextBus2
                addPublicBuses(e2, o)
                val e3 = o.nextBus3
                addPublicBuses(e3, o)
                LogHelper.i(TAG, "Displaying Public Bus Locations for ${o.serviceNo}")
            }
        }

        override fun onReceive(context: Context, intent: Intent) {
            val data = intent.getStringExtra("data") ?: return
            val update = intent.getBooleanExtra("update", false)
            LogHelper.d(TAG, "publicBusReceiver onReceive: update = $update, data length = ${data.length}")
            if (!update) {
                processBusStopData(context, data)
            } else {
                processBusServicesData(context, data)
            }
        }

        private fun getLoadString(load: Int): String {
            return when (load) {
                CommonEnums.BUS_SEATS_AVAIL -> "Seats Available"
                CommonEnums.BUS_STANDING_AVAIL -> "Standing Spots Available"
                CommonEnums.BUS_LIMITED_SEATS -> "Limited Seats"
                else -> "Unknown"
            }
        }

        private fun addPublicBuses(e1: BusArrivalArrayObjectEstimate?, o: BusArrivalArrayObject) {
            if (e1 != null && e1.estimatedArrival != null) {
                val load = getLoadString(e1.loadInt)
                val bus = BusesUtil.vectorToBitmapDescriptor(R.drawable.ic_bus, this@NtuBusActivity, pBusColor)
                val marker = mMap?.addMarker(
                    MarkerOptions().position(LatLng(e1.latitudeD, e1.longitudeD))
                    .title("${o.serviceNo} (${o.operator})").snippet("$load (${BusesUtil.getType(e1.typeInt)})")
                    .icon(bus))
                marker?.let {
                    publicBusMarkers.add(it)
                }
            }
        }
    }

    companion object {
        private const val TAG = "NTUBusActivity"
        const val RECEIVE_NTU_PUBLIC_BUS_DATA_EVENT = "ReceiveNTUBDataEvent"
        const val REFRESH_TASK = 3000
    }
}
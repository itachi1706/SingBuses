package com.itachi1706.busarrivalsg

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.itachi1706.busarrivalsg.adapters.ViewPagerAdapter
import com.itachi1706.busarrivalsg.databinding.ActivityAddBusStopTabbedBinding
import com.itachi1706.busarrivalsg.fragments.BusStopsNearbyFragment
import com.itachi1706.busarrivalsg.fragments.BusStopsSearchFragment
import com.itachi1706.busarrivalsg.services.LocManager
import com.itachi1706.busarrivalsg.util.StaticVariables
import com.itachi1706.helperlib.helpers.EdgeToEdgeHelper
import com.itachi1706.helperlib.helpers.LogHelper

class BusStopsTabbedActivity : AppCompatActivity() {

    private var binding: ActivityAddBusStopTabbedBinding? = null

    private var longitude: Double = 0.0
    private var latitude: Double = 0.0

    private var gps: LocManager? = null
    private var mAnalytics: FirebaseAnalytics? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddBusStopTabbedBinding.inflate(layoutInflater)
        EdgeToEdgeHelper.setEdgeToEdgeWithContentView(binding?.root!!, this)

        setSupportActionBar(binding?.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupViewPager(binding?.mainViewpager)
        binding?.mainTablayout?.setupWithViewPager(binding?.mainViewpager)
        binding?.mainTablayout?.tabGravity = TabLayout.GRAVITY_FILL
        binding?.mainTablayout?.tabMode = TabLayout.MODE_FIXED

        mAnalytics = FirebaseAnalytics.getInstance(this)
    }

    private fun setupViewPager(viewPager: ViewPager?) {
        val adapter = ViewPagerAdapter(supportFragmentManager)

        adapter.addFrag(BusStopsSearchFragment(), "Search")
        adapter.addFrag(BusStopsNearbyFragment(), "Nearby")

        viewPager?.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        initLocationManager()
        binding?.currentLocationFab?.setOnClickListener {
            checkIfYouHaveGpsPermissionForThis()
        }
    }

    private fun initLocationManager() {
        if (gps == null && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gps = LocManager(this)
        }
        gps?.let {
            if (!it.canGetLocation) {
                it.showSettingsAlert()
            }
        }
    }

    private fun checkIfYouHaveGpsPermissionForThis() {
        val rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (rc == PackageManager.PERMISSION_GRANTED) {
            getLocationButtonClicked()
        } else {
            requestGpsPermission()
        }
    }

    private fun requestGpsPermission() {
        LogHelper.w(TAG, "GPS permission is not granted. Proceeding to request")
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            requestGps.launch(permissions)
            return
        }

        MaterialAlertDialogBuilder(this).setTitle(R.string.dialog_title_request_permission_gps)
            .setMessage(R.string.dialog_message_request_permission_gps_rationale)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                requestGps.launch(permissions)
            }.show()
    }

    private fun getLocationButtonClicked() {
        Toast.makeText(applicationContext, R.string.toast_message_retrieving_location, Toast.LENGTH_SHORT).show()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gps?.getLocationNow()
        }
        latitude = gps?.latitude ?: 0.0
        longitude = gps?.longitude ?: 0.0

        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Lat: $latitude | Lng: $longitude")
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "reqCurrentLocation")
        mAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
        updateList()
    }

    private val requestGps = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        val hasPerm = StaticVariables.checkIfCoarseLocationGranted(it)

        if (hasPerm) {
            LogHelper.d(TAG, "Location permission granted - initialize gps source")
            // Has permission
            initLocationManager()
            getLocationButtonClicked()
        } else {
            LogHelper.e(TAG, "Location permission NOT granted")
            MaterialAlertDialogBuilder(this).setTitle(R.string.dialog_title_permission_denied)
                .setMessage(R.string.dialog_message_no_permission_gps)
                .setPositiveButton(android.R.string.ok, null)
                .setNeutralButton(R.string.dialog_action_neutral_app_settings) { _, _ ->
                    val permIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val packageUri = "package:${applicationContext.packageName}".toUri()
                    permIntent.data = packageUri
                    startActivity(permIntent)
                }.show()
        }
    }

    private fun updateList() {
        binding?.mainTablayout?.getTabAt(1)?.select()
        val location = Location("")
        location.latitude = latitude
        location.longitude = longitude

        hideSoftKeyboard()

        val lIntent = Intent(BusStopsNearbyFragment.RECEIVE_LOCATION_EVENT)
        lIntent.putExtra("lat", latitude)
        lIntent.putExtra("lng", longitude)
        LocalBroadcastManager.getInstance(this).sendBroadcast(lIntent)
    }

    private fun hideSoftKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
        LogHelper.i(TAG, "IMM: Attempting to hide keyboard")

        imm?.let {
            // Verify if soft keyboard open
            if (currentFocus != null && (it.isActive || it.isAcceptingText)) {
                LogHelper.i(TAG, "IMM: Hiding keyboard")
                imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        gps?.stopUsingGPS()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_add_bus_stops, menu)
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val TAG = "BusStopsTabbedActivity"
    }
}
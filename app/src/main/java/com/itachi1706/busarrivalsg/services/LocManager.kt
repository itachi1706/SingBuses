package com.itachi1706.busarrivalsg.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.IBinder
import android.provider.Settings
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import com.itachi1706.busarrivalsg.R
import com.itachi1706.helperlib.helpers.LogHelper
import com.itachi1706.helperlib.helpers.LogHelper.e


@SuppressLint("MissingPermission")
class LocManager(val mContext: Context) : Service(), LocationListener {

    var locationManager: LocationManager? = null

    var isGPSEnabled: Boolean = false
    var canGetLocation: Boolean = false
        private set

    var isNetworkEnabled: Boolean = false

    var location: Location? = null

    var latitude: Double = 0.0
        get() = if (location != null) location!!.latitude else field
        private set
    var longitude: Double = 0.0
        get() = if (location != null) location!!.longitude else field
        private set

    var gpsLoc: Location? = null
        private set
    var netLoc: Location? = null // For dev debug setting
        private set

    init {
        getLocationNow()
    }

    /**
     * Gets current location of the user
     * @return user's current location
     */
    @RequiresPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    fun getLocationNow(): Location? {
        try {
            locationManager = mContext.getSystemService(LOCATION_SERVICE) as LocationManager
            if (locationManager == null) {
                e(TAG, "There are no location services available on this device")
                return null
            }

            // Get GPS status
            isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)

            // Get network status
            isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGPSEnabled && !isNetworkEnabled) {
                // No network provider is enabled
                e(TAG, "No provider enabled")
            } else {
                this.canGetLocation = true
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager!!.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES,
                        this
                    )
                    LogHelper.d(TAG, "Network")

                    location =
                        locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if (location != null) {
                        latitude = location!!.latitude
                        longitude = location!!.longitude
                    }
                    netLoc = location
                }
                // If GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled && locationManager != null) {
                    locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this)
                    LogHelper.d(TAG, "GPS Enabled")

                    val gpsLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (location == null) {
                        location = gpsLocation
                    } else {
                        // Check time and get later one as it is more accurate
                        if ((gpsLocation?.time ?: 0) > (location?.time ?: 0)) {
                            location = gpsLocation
                        }
                    }

                    gpsLoc = gpsLocation
                    if (location != null) {
                        latitude = location!!.latitude
                        longitude = location!!.longitude
                    }
                }
            }
        } catch (e: Exception) {
            e(TAG, "Error getting location, ${e.message}", e)
        }

        return location
    }

    /**
     * Function to show settings alert dialog
     */
    fun showSettingsAlert() {
        AlertDialog.Builder(mContext).setTitle(R.string.dialog_title_gps_disabled)
            .setMessage(R.string.dialog_message_gps_disabled)
            .setPositiveButton(R.string.dialog_action_positive_settings, { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                mContext.startActivity(intent)
            }).setNegativeButton(
                R.string.dialog_action_negative_cancel,
                { dialog, _ -> dialog.cancel() }).show()
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     * */
    fun stopUsingGPS() {
        if (locationManager != null) {
            locationManager!!.removeUpdates(this)
        }
    }

    override fun onLocationChanged(location: Location) {
        // NO-OP
    }

    override fun onProviderEnabled(provider: String) {
        // NO-OP
    }

    override fun onProviderDisabled(provider: String) {
        // NO-OP
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        const val TAG = "LocManager"

        // The minimum distance to change Updates in meters
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Float = 10f // 10 meters
        // The minimum time between updates in milliseconds
        private const val MIN_TIME_BW_UPDATES: Long = 1000 * 30L // 30 seconds;
    }


}
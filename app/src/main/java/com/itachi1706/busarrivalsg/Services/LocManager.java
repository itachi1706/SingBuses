package com.itachi1706.busarrivalsg.Services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;

import com.itachi1706.busarrivalsg.R;

/**
 * Created by Kenneth on 20/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.Services
 */
@SuppressWarnings("ResourceType")
public class LocManager extends Service implements LocationListener {

    private final Context mContext;
    public static final String TAG = "LocManager";

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 30; // 30 seconds

    // Declaring a Location Manager
    protected LocationManager locationManager;

    @SuppressWarnings("unused")
    public LocManager() {
        // Default Constructor (UNUSED)
        this.mContext = getApplicationContext();
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public LocManager(Context context) {
        this.mContext = context;
        getLocation();
    }

    /**
     * Gets current location of the user
     * @return user's current location
     */
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            if (locationManager == null) {
                Log.e(TAG, "There are no location service on device");
            }

            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no netAwork provider is enabled
                Log.e(TAG, "No provider enabled");
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d(TAG, "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                        netLoc = location;
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d(TAG, "GPS Enabled");
                    if (locationManager != null) {
                        Location gpslocation = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location == null) location = gpslocation;
                        else {
                            // Check time and get the later one as it will be more accurate
                            if (gpslocation.getTime() > location.getTime())
                                location = gpslocation;
                        }
                        gpsLoc = gpslocation;
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    /**
     * Gets the Latitude value of the user's current position
     * @return a double containing the latitude value
     */
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     * Gets the Longitude value of the user's current position
     * @return a double containing the longitude value
     */
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    /**
     * Function to check if best network provider
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * Function to show settings alert dialog
     */
    public void showSettingsAlert(){
        if (mContext == null) {
            Log.e(TAG, "Invalid Context");
            return;
        }

        new AlertDialog.Builder(mContext).setTitle(R.string.dialog_title_gps_disabled)
                .setMessage(R.string.dialog_message_gps_disabled)
                .setPositiveButton(R.string.dialog_action_positive_settings, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    mContext.startActivity(intent);
                }).setNegativeButton(R.string.dialog_action_negative_cancel, (dialog, which) -> dialog.cancel()).show();
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     * */
    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(LocManager.this);
        }
    }


    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Location gpsLoc = null, netLoc = null; // For dev debug settings

    @Nullable
    public Location getGpsLoc() {
        return gpsLoc;
    }

    @Nullable
    public Location getNetLoc() {
        return netLoc;
    }
}

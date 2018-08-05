package com.itachi1706.busarrivalsg;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.itachi1706.busarrivalsg.Fragments.BusStopSearchFragment;
import com.itachi1706.busarrivalsg.Services.GPSManager;

public class BusStopsTabbedActivity extends AppCompatActivity {

    Toolbar toolbar;
    ViewPager pager;
    TabLayout tabLayout;
    SharedPreferences sp;

    FloatingActionButton currentLocationGet;
    double longitude, latitude;
    GPSManager gps;
    FirebaseAnalytics mAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_bus_stop_tabbed);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        pager = findViewById(R.id.main_viewpager);
        tabLayout = findViewById(R.id.main_tablayout);

        setupViewPager(pager);
        tabLayout.setupWithViewPager(pager);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        currentLocationGet = findViewById(R.id.current_location_fab);
        mAnalytics = FirebaseAnalytics.getInstance(this);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFrag(new BusStopSearchFragment(), "Search");
        adapter.addFrag(new BusStopSearchFragment(), "Nearby");

        viewPager.setAdapter(adapter);
    }

    private static final int RC_HANDLE_ACCESS_FINE_LOCATION = 2;
    private static final int RC_HANDLE_ACCESS_FINE_LOCATION_INIT = 4;

    @Override
    public void onResume() {
        super.onResume();

        int rc = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            //Go ahead and init
            gps = new GPSManager(this);
            if (!gps.canGetLocation()) {
                gps.showSettingsAlert();
            }
        } else {
            requestGpsPermission(RC_HANDLE_ACCESS_FINE_LOCATION_INIT);
        }

        currentLocationGet.setOnClickListener(v -> checkIfYouHaveGpsPermissionForThis());
    }

    private void checkIfYouHaveGpsPermissionForThis() {
        int rc = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            getLocationButtonClicked();
        } else {
            requestGpsPermission(RC_HANDLE_ACCESS_FINE_LOCATION);
        }
    }

    private void requestGpsPermission(final int code) {
        Log.w("GPSManager", "GPS permission is not granted. Requesting permission");
        final String[] permissions = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this, permissions, code);
            return;
        }

        if (code == RC_HANDLE_ACCESS_FINE_LOCATION_INIT)
            return;

        final Activity thisActivity = this;

        new AlertDialog.Builder(this).setTitle(R.string.dialog_title_request_permission_gps)
                .setMessage(R.string.dialog_message_request_permission_gps_rationale)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> ActivityCompat.requestPermissions(thisActivity, permissions, code)).show();
    }

    private void getLocationButtonClicked() {
        Toast.makeText(getApplicationContext(), R.string.toast_message_retrieving_location, Toast.LENGTH_SHORT).show();
        latitude = gps.getLatitude();
        longitude = gps.getLongitude();
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Lat: " + latitude + " | Lng: " + longitude);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "reqCurrentLocation");
        mAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        updateList();
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_ACCESS_FINE_LOCATION && requestCode != RC_HANDLE_ACCESS_FINE_LOCATION_INIT) {
            Log.d("GPSManager", "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("GPSManager", "Location permission granted - initialize the gps source");
            // we have permission
            if (gps == null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return; // Should never happen as it should have been granted
                gps = new GPSManager(this);
            }
            if (!gps.canGetLocation()){
                gps.showSettingsAlert();
            }

            if (requestCode == RC_HANDLE_ACCESS_FINE_LOCATION){
                getLocationButtonClicked();
            }
            return;
        }

        Log.e("GPSManager", "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
        final Activity thisActivity = this;

        if (requestCode == RC_HANDLE_ACCESS_FINE_LOCATION) {
            new AlertDialog.Builder(this).setTitle(R.string.dialog_title_permission_denied)
                    .setMessage(R.string.dialog_message_no_permission_gps).setPositiveButton(android.R.string.ok, null)
                    .setNeutralButton(R.string.dialog_action_neutral_app_settings, (dialog, which) -> {
                        Intent permIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri packageURI = Uri.parse("package:" + thisActivity.getPackageName());
                        permIntent.setData(packageURI);
                        startActivity(permIntent);
                    }).show();
        }
    }

    private void updateList(){
        // TODO: Get location and send location to nearby fragment
        Location location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);

        Intent lIntent = new Intent("ReceiveLocationEvent");
        lIntent.putExtra("lat", latitude);
        lIntent.putExtra("lng", longitude);
        LocalBroadcastManager.getInstance(this).sendBroadcast(lIntent);
    }

    @Override
    public void onPause(){
        super.onPause();
        if (gps != null)
            gps.stopUsingGPS();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_bus_stops, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, MainSettings.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

package com.itachi1706.busarrivalsg;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.itachi1706.busarrivalsg.Fragments.BusStopNearbyFragment;
import com.itachi1706.busarrivalsg.Fragments.BusStopSearchFragment;
import com.itachi1706.busarrivalsg.Services.LocManager;
import com.itachi1706.busarrivalsg.util.StaticVariables;
import com.itachi1706.helperlib.helpers.LogHelper;

import java.util.Objects;

public class BusStopsTabbedActivity extends AppCompatActivity {

    Toolbar toolbar;
    ViewPager pager;
    TabLayout tabLayout;
    SharedPreferences sp;

    FloatingActionButton currentLocationGet;
    double longitude, latitude;
    LocManager gps;
    FirebaseAnalytics mAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_bus_stop_tabbed);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        adapter.addFrag(new BusStopNearbyFragment(), "Nearby");

        viewPager.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        initLocationManager();
        currentLocationGet.setOnClickListener(v -> checkIfYouHaveGpsPermissionForThis());
    }

    private void initLocationManager() {
        if (gps == null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                return; // Should never happen as it should have been granted
            gps = new LocManager(this);
        }
        if (!gps.canGetLocation()) {
            gps.showSettingsAlert();
        }
    }

    private void checkIfYouHaveGpsPermissionForThis() {
        int rc = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            getLocationButtonClicked();
        } else {
            requestGpsPermission();
        }
    }

    private void requestGpsPermission() {
        LogHelper.w(LocManager.TAG, "GPS permission is not granted. Requesting permission");
        final String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
            requestGps.launch(permissions);
            return;
        }

        new AlertDialog.Builder(getApplicationContext()).setTitle(R.string.dialog_title_request_permission_gps)
                .setMessage(R.string.dialog_message_request_permission_gps_rationale)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> requestGps.launch(permissions)).show();
    }

    private void getLocationButtonClicked() {
        Toast.makeText(getApplicationContext(), R.string.toast_message_retrieving_location, Toast.LENGTH_SHORT).show();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            gps.getLocation();
        latitude = gps.getLatitude();
        longitude = gps.getLongitude();
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Lat: " + latitude + " | Lng: " + longitude);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "reqCurrentLocation");
        mAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        updateList();
    }

    private final ActivityResultLauncher<String[]> requestGps = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                boolean hasPerm = StaticVariables.INSTANCE.checkIfCoraseLocationGranted(result);

                if (hasPerm) {
                    LogHelper.d(LocManager.TAG, "Location permission granted - initialize the gps source");
                    // we have permission
                    initLocationManager();
                    getLocationButtonClicked();
                } else {
                    LogHelper.e(LocManager.TAG, "Permission not granted");
                    new AlertDialog.Builder(getApplicationContext()).setTitle(R.string.dialog_title_permission_denied)
                            .setMessage(R.string.dialog_message_no_permission_gps).setPositiveButton(android.R.string.ok, null)
                            .setNeutralButton(R.string.dialog_action_neutral_app_settings, (dialog, which) -> {
                                Intent permIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri packageURI = Uri.parse("package:" + getApplicationContext().getPackageName());
                                permIntent.setData(packageURI);
                                startActivity(permIntent);
                            }).show();
                }
            });

    private void updateList(){
        Objects.requireNonNull(tabLayout.getTabAt(1)).select();
        Location location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);

        hideSoftKeyBoard();

        Intent lIntent = new Intent(BusStopNearbyFragment.RECEIVE_LOCATION_EVENT);
        lIntent.putExtra("lat", latitude);
        lIntent.putExtra("lng", longitude);
        LocalBroadcastManager.getInstance(this).sendBroadcast(lIntent);
    }

    private void hideSoftKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        LogHelper.i("IMM", "Attempting to hide keyboard");

        if(imm != null && getCurrentFocus() != null && (imm.isActive() || imm.isAcceptingText())) { // verify if the soft keyboard is open
            LogHelper.i("IMM", "Hiding Keyboard");
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
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

        if (id == R.id.action_settings) startActivity(new Intent(this, MainSettings.class));
         else if (id == android.R.id.home) finish();
        else return super.onOptionsItemSelected(item);

        return true;
    }
}

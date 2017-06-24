package com.itachi1706.busarrivalsg;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.itachi1706.busarrivalsg.AsyncTasks.PopulateListWithCurrentLocationRecycler;
import com.itachi1706.busarrivalsg.Database.BusStopsDB;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusStopJSON;
import com.itachi1706.busarrivalsg.RecyclerViews.BusStopRecyclerAdapter;
import com.itachi1706.busarrivalsg.Services.GPSManager;

import java.util.ArrayList;

public class AddBusStopsRecyclerActivity extends AppCompatActivity {

    FloatingActionButton currentLocationGet;
    RecyclerView result;
    EditText textLane;

    GPSManager gps;
    FirebaseAnalytics mAnalytics;

    double longitude, latitude;

    BusStopRecyclerAdapter adapter;

    private BusStopsDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bus_stops_recycler);

        currentLocationGet = (FloatingActionButton) findViewById(R.id.current_location_fab);
        result = (RecyclerView) findViewById(R.id.rvNearestBusStops);
        if (result != null) result.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        result.setLayoutManager(linearLayoutManager);
        result.setItemAnimator(new DefaultItemAnimator());
        mAnalytics = FirebaseAnalytics.getInstance(this);

        adapter = new BusStopRecyclerAdapter(new ArrayList<BusStopJSON>());
        result.setAdapter(adapter);

        // Populate with blank
        db = new BusStopsDB(this);
        ArrayList<BusStopJSON> results = db.getAllBusStops();
        adapter.updateAdapter(results);
        adapter.notifyDataSetChanged();

        textLane = (EditText) findViewById(R.id.inputData);
        TextWatcher inputWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString();
                Log.d("TextWatcher", "Query searched: " + query);
                ArrayList<BusStopJSON> results = db.getBusStopsByQuery(query);
                if (results != null) {
                    Log.d("TextWatcher", "Finished Search. Size: " + results.size());
                    adapter.updateAdapter(results);
                    adapter.notifyDataSetChanged();
                }
            }
        };
        textLane.addTextChangedListener(inputWatcher);
    }


    private static final int RC_HANDLE_ACCESS_FINE_LOCATION = 2;
    private static final int RC_HANDLE_ACCESS_FINE_LOCATION_INIT = 4;

    @Override
    public void onResume(){
        super.onResume();

        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (rc == PackageManager.PERMISSION_GRANTED){
            //Go ahead and init
            gps = new GPSManager(this);
            if (!gps.canGetLocation()){
                gps.showSettingsAlert();
            }
        } else {
            requestGpsPermission(RC_HANDLE_ACCESS_FINE_LOCATION_INIT);
        }

        currentLocationGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIfYouHaveGpsPermissionForThis();
            }
        });
    }

    private void checkIfYouHaveGpsPermissionForThis(){
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (rc == PackageManager.PERMISSION_GRANTED){
            getLocationButtonClicked();
        } else {
            requestGpsPermission(RC_HANDLE_ACCESS_FINE_LOCATION);
        }
    }

    private void requestGpsPermission(final int code) {
        Log.w("GPSManager", "GPS permission is not granted. Requesting permission");
        final String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
            ActivityCompat.requestPermissions(this, permissions, code);
            return;
        }

        if (code == RC_HANDLE_ACCESS_FINE_LOCATION_INIT)
            return;

        final Activity thisActivity = this;

        new AlertDialog.Builder(this).setTitle(R.string.dialog_title_request_permission_gps)
                .setMessage(R.string.dialog_message_request_permission_gps_rationale)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(thisActivity, permissions, code);
                    }
                }).show();
    }

    private void getLocationButtonClicked(){
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
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_ACCESS_FINE_LOCATION && requestCode != RC_HANDLE_ACCESS_FINE_LOCATION_INIT){
            Log.d("GPSManager", "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("GPSManager", "Location permission granted - initialize the gps source");
            // we have permission, so create the camerasource
            if (gps != null){
                if (!gps.canGetLocation()){
                    gps.showSettingsAlert();
                }
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
                    .setNeutralButton(R.string.dialog_action_neutral_app_settings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent permIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri packageURI = Uri.parse("package:" + thisActivity.getPackageName());
                            permIntent.setData(packageURI);
                            startActivity(permIntent);
                        }
                    }).show();
        }
    }

    private void updateList(){
        BusStopsDB db = new BusStopsDB(this);
        Location location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        new PopulateListWithCurrentLocationRecycler(this, db, adapter).execute(location);
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

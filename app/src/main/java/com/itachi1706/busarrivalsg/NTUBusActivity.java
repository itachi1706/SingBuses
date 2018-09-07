package com.itachi1706.busarrivalsg;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.itachi1706.busarrivalsg.AsyncTasks.GetNTUData;
import com.itachi1706.busarrivalsg.GsonObjects.ntubuses.NTUBus;
import com.itachi1706.busarrivalsg.Services.LocManager;

import java.util.ArrayList;
import java.util.List;

public class NTUBusActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    Switch campusRed, campusBlue, campusRider, campusWeekend;
    MapView mapView;
    private GoogleMap mMap;

    private static final String TAG = "NTUBus";

    public static final String RECEIVE_NTU_DATA_EVENT = "RecieveNTUDataEvent";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ntubus);

        mapView = findViewById(R.id.mapView);
        campusRed = findViewById(R.id.ntu_clr_switch);
        campusBlue = findViewById(R.id.ntu_clb_switch);
        campusRider = findViewById(R.id.ntu_cr_switch);
        campusWeekend = findViewById(R.id.ntu_crw_switch);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        Log.i(TAG, "Creating Map");
    }

    @Override
    public void onResume() {
        super.onResume();

        mapView.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(RECEIVE_NTU_DATA_EVENT));
    }

    @Override
    public void onPause() {
        super.onPause();

        mapView.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady()");
        mMap = googleMap;
        mMap.setTrafficEnabled(true);
        checkIfYouHaveGpsPermissionForThis();
        mMap.setOnInfoWindowClickListener(this);
        UiSettings settings = mMap.getUiSettings();
        settings.setZoomControlsEnabled(true);
        settings.setMapToolbarEnabled(false);

        Log.d(TAG, "Map Created");

        // TODO: Test
        new GetNTUData(this, false).execute("red");
    }


    private static final int RC_HANDLE_ACCESS_FINE_LOCATION = 5;

    private void checkIfYouHaveGpsPermissionForThis() {
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            requestGpsPermission(RC_HANDLE_ACCESS_FINE_LOCATION);
        }
    }

    private void requestGpsPermission(final int code) {
        Log.w(LocManager.TAG, "GPS permission is not granted. Requesting permission");
        final String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this, permissions, code);
            return;
        }

        final Activity thisActivity = this;

        new AlertDialog.Builder(this).setTitle(R.string.dialog_title_request_permission_gps)
                .setMessage(R.string.dialog_message_request_permission_gps_view_map_rationale)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> ActivityCompat.requestPermissions(thisActivity, permissions, code)).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_ACCESS_FINE_LOCATION) {
            Log.d(LocManager.TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(LocManager.TAG, "Location permission granted - enabling my location");
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mMap.setMyLocationEnabled(true);
            }
            return;
        }

        Log.e(LocManager.TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
        Toast.makeText(this, "No Permission for current location", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        // TODO: Click Info Window when created
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO: Parse to Gson and handle the plotting (do in main thread first, we might do in async in the future
            // TODO: Support multiple routes (Now only supports 1 color, which will confuse people)
            String data = intent.getStringExtra("data");
            if (data == null) return;
            Gson gson = new Gson();
            NTUBus busObj = gson.fromJson(data, NTUBus.class);
            if (busObj.getRoutes().length <= 0) return;

            @Nullable NTUBus.MapPoints centerOn = null;
            if (busObj.getRoutes() != null) {
                List<LatLng> mapToDraw = new ArrayList<>();
                for (NTUBus.Route r : busObj.getRoutes()) {
                    if (r.getRoute().getCenter().length > 0)
                        centerOn = r.getRoute().getCenter()[0];
                   for (NTUBus.MapNodes node : r.getRoute().getNodes()) {
                        mapToDraw.add(new LatLng(node.getLat(), node.getLon()));
                        if (node.getPoints().length > 0) {
                            for (NTUBus.MapPoints p : node.getPoints()) {
                                mapToDraw.add(new LatLng(p.getLat(), p.getLon()));
                            }
                        }
                   }
                }

                // Draw on Map Object
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.addAll(mapToDraw);
                polylineOptions.width(10);
                polylineOptions.color(Color.RED);
                mMap.addPolyline(polylineOptions);

                Log.i(TAG, "Generated CL-R route");

            }

            if (centerOn != null) {
                LatLng myLatLng = new LatLng(centerOn.getLat(), centerOn.getLon());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 15));
            }
        }
    };
}

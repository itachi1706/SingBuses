package com.itachi1706.busarrivalsg;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.itachi1706.busarrivalsg.AsyncTasks.GetNTUData;
import com.itachi1706.busarrivalsg.Services.LocManager;
import com.itachi1706.busarrivalsg.Util.BusesUtil;
import com.itachi1706.busarrivalsg.Util.StaticVariables;
import com.itachi1706.busarrivalsg.objects.gson.ntubuses.NTUBus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class NTUBusActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    Switch campusRed, campusBlue, campusRider, campusWeekend, traffic;
    MapView mapView;
    private GoogleMap mMap;

    private static final String TAG = "NTUBus";

    public static final String RECEIVE_NTU_DATA_EVENT = "RecieveNTUDataEvent";

    private BusesUtil busesUtil = BusesUtil.INSTANCE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ntubus);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            Bitmap home = busesUtil.vectorToBitmap(R.drawable.ic_ntu_coa, getResources(), null);
            Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(home, (int) busesUtil.pxFromDp(26, getResources()),
                    (int) busesUtil.pxFromDp(32, getResources()), true));
            getSupportActionBar().setHomeAsUpIndicator(d);
        }

        mapView = findViewById(R.id.mapView);
        campusRed = findViewById(R.id.ntu_clr_switch);
        campusBlue = findViewById(R.id.ntu_clb_switch);
        campusRider = findViewById(R.id.ntu_cr_switch);
        campusWeekend = findViewById(R.id.ntu_crw_switch);
        traffic = findViewById(R.id.ntu_traffic_switch);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        Log.i(TAG, "Creating Map");

        trafficEnabled = traffic.isChecked();
        traffic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            trafficEnabled = isChecked;
            mMap.setTrafficEnabled(trafficEnabled);
        });

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        // Manually set checked state
        campusRed.setChecked(sp.getBoolean("ntu_bus_red", false));
        campusBlue.setChecked(sp.getBoolean("ntu_bus_blue", false));
        campusRider.setChecked(sp.getBoolean("ntu_bus_green", false));
        campusWeekend.setChecked(sp.getBoolean("ntu_bus_brown", false));

        campusRed.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sp.edit().putBoolean("ntu_bus_red", isChecked).apply();
            getData(false);
        });
        campusBlue.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sp.edit().putBoolean("ntu_bus_blue", isChecked).apply();
            getData(false);
        });
        campusRider.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sp.edit().putBoolean("ntu_bus_green", isChecked).apply();
            getData(false);
        });
        campusWeekend.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sp.edit().putBoolean("ntu_bus_brown", isChecked).apply();
            getData(false);
        });
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ntu_buses, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, MainSettings.class));
                return true;
            case android.R.id.home:
                finish();
                return true;
            case R.id.refresh:
                Log.i("NTUBus", "Manually refreshing bus data at " + StaticVariables.convertDateToString(new Date(System.currentTimeMillis())));
                getData(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean trafficEnabled = false;
    private boolean mapReady = false;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady()");
        mMap = googleMap;
        mMap.setTrafficEnabled(trafficEnabled);
        checkIfYouHaveGpsPermissionForThis();
        mMap.setOnInfoWindowClickListener(this);
        UiSettings settings = mMap.getUiSettings();
        settings.setZoomControlsEnabled(true);
        settings.setMapToolbarEnabled(false);
        mapReady = true;

        Log.d(TAG, "Map Created");

        // Enable all toggles
        campusWeekend.setEnabled(true);
        campusRider.setEnabled(true);
        campusRed.setEnabled(true);
        campusBlue.setEnabled(true);
        traffic.setEnabled(true);

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(1.3478184567642855, 103.68342014685716), 15.4f)); // Hardcode center of school
        refreshHandler = new Handler();
        getData(false);
    }

    private void getData(boolean refresh) {
        List<String> get = new ArrayList<>();
        if (campusRider.isChecked()) get.add("green");
        if (campusRed.isChecked()) get.add("red");
        if (campusBlue.isChecked()) get.add("blue");
        if (campusWeekend.isChecked()) get.add("brown");

        if (!mapReady) return;
        if (get.isEmpty()) {
            mMap.clear();
            return;
        }
        if (!refresh) {
            campusRed.setEnabled(false);
            campusBlue.setEnabled(false);
            campusRider.setEnabled(false);
            campusWeekend.setEnabled(false);
        }
        new GetNTUData(this, refresh).execute(get.toArray(new String[get.size()]));
        // TODO: Let user change delay in settings
        if (!refreshHandler.hasMessages(3000)) {
            Message ref = Message.obtain(refreshHandler, refreshTask);
            ref.what = 3000;
            refreshHandler.sendMessageDelayed(ref, 10000); // 10 seconds auto-refresh if theres routes selected
        }
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
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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

    private ArrayList<Marker> busMarkers = new ArrayList<>();

    private Handler refreshHandler;
    private Runnable refreshTask = () -> {
        Log.i("NTUBus", "Auto-refreshing bus data at " + StaticVariables.convertDateToString(new Date(System.currentTimeMillis())));
        getData(true);
    };

    // Draw the route first
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO: Parse to Gson and handle the plotting (do in main thread first, we might do in async in the future)
            String data = intent.getStringExtra("data");
            int update = intent.getIntExtra("update", 0);
            if (data == null) return;
            Gson gson = new Gson();
            NTUBus busObj = gson.fromJson(data, NTUBus.class);
            if (busObj == null) return;
            assert busObj.getRoutes() != null;
            if (busObj.getRoutes().length <= 0) return;

            @Nullable NTUBus.MapPoints centerOn = null;
            if (update == 0) {
                mMap.clear();
                busMarkers.clear();

                if (busObj.getRoutes() != null) {
                    List<LatLng> mapToDraw = new ArrayList<>();
                    for (NTUBus.Route r : busObj.getRoutes()) {
                        if (r.getRoute() != null) {
                            mapToDraw.clear();
                            assert r.getRoute().getCenter() != null;
                            assert r.getRoute().getNodes() != null;
                            if (r.getRoute().getCenter().length > 0)
                                centerOn = r.getRoute().getCenter()[0];
                            BitmapDescriptor stop = busesUtil.vectorToBitmapDescriptor(R.drawable.ic_circle, getResources(), getRouteColor(r.getId()));
                            for (NTUBus.MapNodes node : r.getRoute().getNodes()) {
                                if (node.is_stop_point()) {
                                    mMap.addMarker(new MarkerOptions().position(new LatLng(node.getLat(), node.getLon()))
                                            .title(node.getName())
                                            .snippet("Next Stop: " + node.getShort_direction())
                                            .icon(stop));
                                }
                                assert node.getPoints() != null;
                                if (node.getPoints().length > 0) {
                                    for (NTUBus.MapPoints p : node.getPoints()) {
                                        mapToDraw.add(new LatLng(p.getLat(), p.getLon()));
                                    }
                                }
                            }

                            // Check for bus objects
                            busMarkers.addAll(addBusesIntoRoute(r));

                            // Draw on Map Object
                            PolylineOptions polylineOptions = new PolylineOptions();
                            polylineOptions.addAll(mapToDraw);
                            polylineOptions.width(10);
                            // Set Colors
                            polylineOptions.color(getRouteColor(r.getId()));
                            mMap.addPolyline(polylineOptions);

                            Log.i(TAG, "Generated " + r.getRoutename());
                        }
                    }
                }

            } else {
                // Only update buses
                for (Marker m : busMarkers) {
                    m.remove();
                }
                busMarkers.clear();
                if (busObj.getRoutes() != null) {
                    for (NTUBus.Route r : busObj.getRoutes()) {
                        busMarkers.addAll(addBusesIntoRoute(r));
                    }
                }
                Toast.makeText(context, "Refreshed", Toast.LENGTH_SHORT).show(); // TODO: Test code
            }

            campusWeekend.setEnabled(true);
            campusRider.setEnabled(true);
            campusRed.setEnabled(true);
            campusBlue.setEnabled(true);

            LatLng myLatLng;
            if (centerOn != null) myLatLng = new LatLng(centerOn.getLat(), centerOn.getLon());
            else
                myLatLng = new LatLng(1.3478184567642855, 103.68342014685716); // Hardcode center of school
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 15.4f));
        }
    };

    private List<Marker> addBusesIntoRoute(NTUBus.Route r) {
        List<Marker> markers = new ArrayList<>();
        if (r.getVehicles() != null && r.getVehicles().length > 0) {
            BitmapDescriptor bus = busesUtil.vectorToBitmapDescriptor(R.drawable.ic_bus, getResources(), getRouteColor(r.getId()));
            for (NTUBus.Vehicles v : r.getVehicles()) {
                // TODO: Find a way to do the bearing lol
                                /*Bitmap arrow = busesUtil.vectorToBitmap(R.drawable.ic_chevron, getResources(), getRouteColor(r.getId()));
                                Matrix matrix = new Matrix();
                                matrix.setRotate(v.getBearing());
                                arrow = Bitmap.createBitmap(arrow, 0, 0, arrow.getWidth(), arrow.getHeight(), matrix, true);*/
                markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(v.getLatVal(), v.getLonVal()))
                        .title(v.getLicense_no()).snippet("Speed: " + v.getSpeed() + " km/h | Bearing: " + v.getBearing())
                        .icon(bus)));
            }
        }
        return markers;
    }

    private int getRouteColor(int id) {
        switch (id) {
            case 44478: return Color.RED;
            case 44479: return Color.BLUE;
            case 44480: return Color.GREEN;
            case 44481: return Color.parseColor("#964B00");
            default: return Color.BLACK;
        }
    }
}

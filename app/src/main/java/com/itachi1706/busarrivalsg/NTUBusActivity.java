package com.itachi1706.busarrivalsg;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.ArrayMap;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.itachi1706.busarrivalsg.AsyncTasks.GetNTUPublicBusData;
import com.itachi1706.busarrivalsg.Services.LocManager;
import com.itachi1706.busarrivalsg.gsonObjects.sgLTA.BusArrivalArrayObject;
import com.itachi1706.busarrivalsg.gsonObjects.sgLTA.BusArrivalArrayObjectEstimate;
import com.itachi1706.busarrivalsg.gsonObjects.sgLTA.BusArrivalMain;
import com.itachi1706.busarrivalsg.gsonObjects.sgLTA.BusStopJSON;
import com.itachi1706.busarrivalsg.objects.CommonEnums;
import com.itachi1706.busarrivalsg.util.BusesUtil;
import com.itachi1706.busarrivalsg.util.OnMapViewReadyListener;
import com.itachi1706.busarrivalsg.util.StaticVariables;
import com.itachi1706.helperlib.concurrent.Constants;
import com.itachi1706.helperlib.concurrent.CoroutineAsyncTask;
import com.itachi1706.helperlib.helpers.LogHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class NTUBusActivity extends AppCompatActivity implements OnMapViewReadyListener.OnGlobalMapReadyListener, GoogleMap.OnInfoWindowClickListener {

    MapView mapView;
    private GoogleMap mMap;

    private static final String TAG = "NTUBus";

    public static final String RECEIVE_NTU_PUBLIC_BUS_DATA_EVENT = "RecieveNTUBDataEvent";

    private static final BusesUtil busesUtil = BusesUtil.INSTANCE;
    private int autoRefreshDelay = -1;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ntubus_with_sheet);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            Bitmap home = busesUtil.vectorToBitmap(R.drawable.ic_ntu_coa, this, null);
            Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(home, (int) busesUtil.pxFromDp(26, getResources()),
                    (int) busesUtil.pxFromDp(32, getResources()), true));
            getSupportActionBar().setHomeAsUpIndicator(d);
        }

        mapView = findViewById(R.id.mapView);

        // Init Map
        mapView.onCreate(savedInstanceState);
        new OnMapViewReadyListener(mapView, this);
        LogHelper.i(TAG, "Creating Map");

        sp = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        mapView.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(publicBusReceiver, new IntentFilter(RECEIVE_NTU_PUBLIC_BUS_DATA_EVENT));

        autoRefreshDelay = Integer.parseInt(sp.getString("ntushuttlerefrate", "10"));
        if (autoRefreshDelay < 5) autoRefreshDelay = 5;
        shouldAutoRefresh = true;
    }

    @Override
    public void onPause() {
        super.onPause();

        mapView.onPause();
        shouldAutoRefresh = false;
        if (refreshHandler != null) refreshHandler.removeMessages(REFRESH_TASK);
        if (publicBusReceiver != null) LocalBroadcastManager.getInstance(this).unregisterReceiver(publicBusReceiver);
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

        if (id == R.id.action_settings) startActivity(new Intent(this, MainSettings.class));
        else if (id == android.R.id.home) finish();
        else if (id == R.id.refresh) {
            LogHelper.i(TAG, "Manually refreshing bus data at " + StaticVariables.INSTANCE.convertDateToString(new Date(System.currentTimeMillis())));
            getData(true);
        } else return super.onOptionsItemSelected(item);

        return true;
    }

    private boolean mapReady = false;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LogHelper.d(TAG, "onMapReady()");
        mMap = googleMap;
        mMap.setTrafficEnabled(true);
        checkIfYouHaveGpsPermissionForThis();
        mMap.setOnInfoWindowClickListener(this);
        UiSettings settings = mMap.getUiSettings();
        settings.setZoomControlsEnabled(true);
        settings.setMapToolbarEnabled(false);
        mapReady = true;

        LogHelper.d(TAG, "Map Created");

        mMap.setOnMapLoadedCallback(() -> mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(1.3478184567642855, 103.68342014685716), 15.0f))); // Hardcode center of school

        refreshHandler = new Handler();
        getData(false);
    }

    private void getData(boolean refresh) {
        if (!mapReady) return;
        if (runningPBus == null || runningPBus.getStatus().equals(Constants.Status.FINISHED) || runningPBus.isCancelled()) {
            runningPBus = new GetNTUPublicBusData(this, refresh);
            runningPBus.execute();
        }
        if (!refreshHandler.hasMessages(REFRESH_TASK) && shouldAutoRefresh) {
            Message ref = Message.obtain(refreshHandler, refreshTask);
            ref.what = REFRESH_TASK;
            refreshHandler.sendMessageDelayed(ref, autoRefreshDelay * 1000L);
        }
    }

    private void checkIfYouHaveGpsPermissionForThis() {
        int rc = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            requestGpsPermission();
        }
    }

    private void requestGpsPermission() {
        LogHelper.w(LocManager.TAG, "GPS permission is not granted. Requesting permission");
        final String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            requestGps.launch(permissions);
            return;
        }

        new AlertDialog.Builder(this).setTitle(R.string.dialog_title_request_permission_gps)
                .setMessage(R.string.dialog_message_request_permission_gps_view_map_rationale)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> requestGps.launch(permissions)).show();
    }

    @SuppressLint("MissingPermission") // This is basically a permission check alr
    private final ActivityResultLauncher<String[]> requestGps = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                boolean hasPerm = StaticVariables.INSTANCE.checkIfCoraseLocationGranted(result);

                if (hasPerm) {
                    LogHelper.d(LocManager.TAG, "Location permission granted - enabling my location");
                    // we have permission, so set my location to enabled
                    mMap.setMyLocationEnabled(true);
                } else {
                    LogHelper.e(LocManager.TAG, "Permission not granted");
                    Toast.makeText(this, "No Permission for current location", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (marker.getTag() == null) return;
        Object type = marker.getTag();
        LogHelper.d("NTU-BUS-MAP", "Indo clicked of " + type.getClass());
        if (type instanceof BusStopJSON) {
            BusStopJSON json = (BusStopJSON) type;
            json.getBusStopCode();
            json.getDescription();

            Intent pBusIntent = new Intent(this, BusServicesAtStopRecyclerActivity.class);
            pBusIntent.putExtra("stopCode", json.getBusStopCode());
            pBusIntent.putExtra("stopName", json.getDescription());
            startActivity(pBusIntent);
        }

    }

    private final ArrayList<Marker> publicBusMarkers = new ArrayList<>();

    private CoroutineAsyncTask<Void, Void, Integer> runningPBus = null;

    private Handler refreshHandler;
    private boolean shouldAutoRefresh = false;
    public static final int REFRESH_TASK = 3000;
    private final Runnable refreshTask = () -> {
        LogHelper.i(TAG, "Auto-refreshing bus data at " + StaticVariables.INSTANCE.convertDateToString(new Date(System.currentTimeMillis())));
        getData(true);
    };

    /**
     * Parsing and processing the data received for public API calls
     */
    private final BroadcastReceiver publicBusReceiver = new BroadcastReceiver() {

        private final int sbsColor = Color.parseColor("#800080");

        @Override
        public void onReceive(Context context, Intent intent) {
            String data = intent.getStringExtra("data");
            boolean update = intent.getBooleanExtra("update", false);
            if (data == null) return;
            Gson gson = new Gson();
            if (!update) {
                BusStopJSON[] tmpJSON;
                try {
                    tmpJSON = gson.fromJson(data, BusStopJSON[].class);
                }catch (JsonSyntaxException e) {
                    Toast.makeText(context, "An error occurred parsing public bus stops. Please try again later", Toast.LENGTH_LONG).show();
                    return;
                }
                // Convert to something workable and unique
                ArrayMap<String, BusStopJSON> busStops = new ArrayMap<>();
                for (BusStopJSON j : tmpJSON) {
                    busStops.put(j.getBusStopCode(), j);
                }
                BitmapDescriptor stop = busesUtil.vectorToBitmapDescriptor(R.drawable.ic_circle, context, sbsColor);
                for (ArrayMap.Entry<String, BusStopJSON> entry : busStops.entrySet()) {
                    BusStopJSON node = entry.getValue();
                    String svcWork = node.getServices();
                    String[] svces = svcWork.split(",");
                    StringBuilder s = new StringBuilder();
                    for (String s1 : svces) {
                        String[] svcTmp = s1.split(":");
                        s.append(svcTmp[0]).append(", ");
                    }
                    String services = s.toString();
                    services = services.replaceAll(", $", "");
                    Marker m = mMap.addMarker(new MarkerOptions().position(new LatLng(node.getLatitude(), node.getLongitude()))
                            .title(node.getDescription() + " (" + node.getRoadName() + ")")
                            .snippet("Bus Svcs: " + services)
                            .icon(stop));
                    if (m != null) {
                        m.setTag(node);
                    }
                    LogHelper.i(TAG, "Generated Public Bus Stops");
                }
            } else {
                BusArrivalMain[] busObjsArr;
                try {
                    busObjsArr = gson.fromJson(data, BusArrivalMain[].class);
                } catch (JsonSyntaxException e) {
                    Toast.makeText(context, "An error occurred parsing public buses. Please try again later", Toast.LENGTH_LONG).show();
                    return;
                }

                for (BusArrivalMain busObjs : busObjsArr) {
                    if (busObjs == null || busObjs.getServices() == null || busObjs.getServices().length == 0) continue;

                    BusArrivalArrayObject o = busObjs.getServices()[0];
                    // Remove bus markers for this specific service
                    Iterator<Marker> iter = publicBusMarkers.iterator();
                    while (iter.hasNext()) {
                        Marker m = iter.next();

                        if (m == null || m.getTitle() == null) continue;

                        if (m.getTitle().equals(o.getServiceNo() + " (" + o.getOperator() + ")")) {
                            m.remove();
                            iter.remove();
                        }
                    }
                    BusArrivalArrayObjectEstimate e1 = o.getNextBus();
                    addPublicBuses(e1, o);
                    BusArrivalArrayObjectEstimate e2 = o.getNextBus2();
                    addPublicBuses(e2, o);
                    BusArrivalArrayObjectEstimate e3 = o.getNextBus3();
                    addPublicBuses(e3, o);
                    LogHelper.i(TAG, "Displaying Public Bus Locations for " + o.getServiceNo());
                }
            }
        }

        private String getLoadString(int load) {
            switch (load){
                case CommonEnums.BUS_SEATS_AVAIL: return "Seats Available";
                case CommonEnums.BUS_STANDING_AVAIL: return "Standing Spots Available";
                case CommonEnums.BUS_LIMITED_SEATS: return "Limited Seats";
                default: return "Unknown";
            }
        }

        private void addPublicBuses(BusArrivalArrayObjectEstimate e1, BusArrivalArrayObject o) {
            if (e1 != null && e1.getEstimatedArrival() != null) {
                String load = getLoadString(e1.getLoadInt());
                BitmapDescriptor bus = busesUtil.vectorToBitmapDescriptor(R.drawable.ic_bus, getApplicationContext(), sbsColor);
                publicBusMarkers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(e1.getLatitudeD(), e1.getLongitudeD()))
                        .title(o.getServiceNo() + " (" + o.getOperator() + ")").snippet(load + " (" + BusesUtil.INSTANCE.getType(e1.getTypeInt()) + ")")
                        .icon(bus)));
            }
        }
    };
}

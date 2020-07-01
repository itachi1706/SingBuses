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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.ArrayMap;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.itachi1706.busarrivalsg.AsyncTasks.GetNTUData;
import com.itachi1706.busarrivalsg.AsyncTasks.GetNTUPublicBusData;
import com.itachi1706.busarrivalsg.AsyncTasks.QueryNTUStops;
import com.itachi1706.busarrivalsg.Services.LocManager;
import com.itachi1706.busarrivalsg.gsonObjects.sgLTA.BusArrivalArrayObject;
import com.itachi1706.busarrivalsg.gsonObjects.sgLTA.BusArrivalArrayObjectEstimate;
import com.itachi1706.busarrivalsg.gsonObjects.sgLTA.BusArrivalMain;
import com.itachi1706.busarrivalsg.gsonObjects.sgLTA.BusStopJSON;
import com.itachi1706.busarrivalsg.objects.CommonEnums;
import com.itachi1706.busarrivalsg.objects.gson.ntubuses.NTUBus;
import com.itachi1706.busarrivalsg.util.BusesUtil;
import com.itachi1706.busarrivalsg.util.NTURouteCacher;
import com.itachi1706.busarrivalsg.util.OnMapViewReadyListener;
import com.itachi1706.busarrivalsg.util.StaticVariables;
import com.itachi1706.helperlib.helpers.LogHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class NTUBusActivity extends AppCompatActivity implements OnMapViewReadyListener.OnGlobalMapReadyListener, GoogleMap.OnInfoWindowClickListener {

    Switch campusRed, campusBlue, campusRider, campusWeekend, traffic, sbs;
    MapView mapView;
    private GoogleMap mMap;

    private static final String TAG = "NTUBus";

    public static final String RECEIVE_NTU_DATA_EVENT = "RecieveNTUDataEvent";
    public static final String RECEIVE_NTU_PUBLIC_BUS_DATA_EVENT = "RecieveNTUBDataEvent";

    private final BusesUtil busesUtil = BusesUtil.INSTANCE;
    private int autoRefreshDelay = -1;
    private SharedPreferences sp;

    private BottomSheetBehavior<View> bottomSheetBehavior;
    private View bottomSheet;

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
        campusRed = findViewById(R.id.ntu_clr_switch);
        campusBlue = findViewById(R.id.ntu_clb_switch);
        campusRider = findViewById(R.id.ntu_cr_switch);
        campusWeekend = findViewById(R.id.ntu_crw_switch);
        sbs = findViewById(R.id.ntu_sbs_switch);
        traffic = findViewById(R.id.ntu_traffic_switch);

        // Init Bottom Sheet
        bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(200);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        // Init Map
        mapView.onCreate(savedInstanceState);
        new OnMapViewReadyListener(mapView, this);
        LogHelper.i(TAG, "Creating Map");

        trafficEnabled = traffic.isChecked();
        traffic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            trafficEnabled = isChecked;
            mMap.setTrafficEnabled(trafficEnabled);
        });

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        // Manually set checked state
        campusRed.setChecked(sp.getBoolean("ntu_bus_red", false));
        campusBlue.setChecked(sp.getBoolean("ntu_bus_blue", false));
        campusRider.setChecked(sp.getBoolean("ntu_bus_green", false));
        campusWeekend.setChecked(sp.getBoolean("ntu_bus_brown", false));
        sbs.setChecked(sp.getBoolean("ntu_bus_sbs", false));

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
        sbs.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sp.edit().putBoolean("ntu_bus_sbs", isChecked).apply();
            getData(false);
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        mapView.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(RECEIVE_NTU_DATA_EVENT));
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
        if (receiver != null) LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
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

    private boolean trafficEnabled = false;
    private boolean mapReady = false;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LogHelper.d(TAG, "onMapReady()");
        mMap = googleMap;
        mMap.setTrafficEnabled(trafficEnabled);
        checkIfYouHaveGpsPermissionForThis();
        mMap.setOnInfoWindowClickListener(this);
        UiSettings settings = mMap.getUiSettings();
        settings.setZoomControlsEnabled(true);
        settings.setMapToolbarEnabled(false);
        mapReady = true;

        LogHelper.d(TAG, "Map Created");

        // Enable all toggles
        campusWeekend.setEnabled(true);
        campusRider.setEnabled(true);
        campusRed.setEnabled(true);
        campusBlue.setEnabled(true);
        traffic.setEnabled(true);

        mMap.setOnMapLoadedCallback(() -> mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(1.3478184567642855, 103.68342014685716), 15.4f))); // Hardcode center of school

        refreshHandler = new Handler();
        getData(false);

        // Bottom Sheet handler
        mMap.setOnMapClickListener(latLng -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN));
        mMap.setOnInfoWindowClickListener(this::updateBottomSheetIfAny);
        mMap.setOnMarkerClickListener(marker -> {
            updateBottomSheetIfAny(marker);
            return true;
        });
    }

    private void updateBottomSheetIfAny(Marker marker) {
        marker.showInfoWindow();
        if (!marker.getSnippet().startsWith("Next Stop: ")) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN); // Make sure its gone
            return; // Don't do anything as it is not stop markers
        }

        TextView main = bottomSheet.findViewById(R.id.detail_name);
        TextView sub = bottomSheet.findViewById(R.id.detail_subtext);
        ProgressBar inProgress = bottomSheet.findViewById(R.id.progress_loading);
        TextView result = bottomSheet.findViewById(R.id.timings);
        sub.setText(marker.getSnippet());
        main.setText(marker.getTitle());
        inProgress.setVisibility(View.VISIBLE);
        result.setVisibility(View.GONE);
        if (marker.getTag() == null || !(marker.getTag() instanceof NTUBus.MapNodes)) {
            // Nothing alr
            result.setText("No Timings Data Found\nDebug Error: Invalid Tag");
            result.setVisibility(View.VISIBLE);
            inProgress.setVisibility(View.GONE);
        } else {
            NTUBus.MapNodes n = (NTUBus.MapNodes) marker.getTag();
            sub.setText(marker.getSnippet() + "\nCurrent Stop ID: " + n.getId());
            if (n.getId() == 0) {
                // This is an error so we tell the user to clear cache if automatic clearing fails
                new NTURouteCacher(this).clearAllCachedFile();
                result.setText("An error has occurred. We have cleared your cache files. Please reopen the application and try again." +
                        "\n\nIf it still fails, try clearing the application cache.\n" +
                        "You can do so by going to your Phone Settings -> Applications -> Bus Arrivals @ SG -> Storage -> Clear Cache\n" +
                        "Reopen the application afterwards and timing data should appear");
                result.setVisibility(View.VISIBLE);
                inProgress.setVisibility(View.GONE);
            } else new QueryNTUStops(this, sub.getText().toString(), (error, resultText, title, subtext) -> {
                if (!error) {
                    main.setText(title);
                    sub.setText(subtext);
                }
                result.setText(resultText);
                inProgress.setVisibility(View.GONE);
                result.setVisibility(View.VISIBLE);
            }).execute(n.getId());
        }
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
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
            if (sbs.isChecked()) new GetNTUPublicBusData(this, false).execute(); // Do not need to autorefresh every few seconds as it doesnt update fast anyway
            return;
        }
        if (!refresh) {
            campusRed.setEnabled(false);
            campusBlue.setEnabled(false);
            campusRider.setEnabled(false);
            campusWeekend.setEnabled(false);
        }
        if (runningBus == null || runningBus.getStatus().equals(AsyncTask.Status.FINISHED) || runningBus.isCancelled())
            runningBus = new GetNTUData(this, refresh).execute(get.toArray(new String[0]));
        if (sbs.isChecked())
            if (runningPBus == null || runningPBus.getStatus().equals(AsyncTask.Status.FINISHED) || runningPBus.isCancelled())
                runningPBus = new GetNTUPublicBusData(this, refresh).execute();
        if (!refreshHandler.hasMessages(REFRESH_TASK) && shouldAutoRefresh) {
            Message ref = Message.obtain(refreshHandler, refreshTask);
            ref.what = REFRESH_TASK;
            refreshHandler.sendMessageDelayed(ref, autoRefreshDelay * 1000);
        }
    }


    private static final int RC_HANDLE_ACCESS_FINE_LOCATION = 5;

    private void checkIfYouHaveGpsPermissionForThis() {
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            requestGpsPermission();
        }
    }

    private void requestGpsPermission() {
        LogHelper.w(LocManager.TAG, "GPS permission is not granted. Requesting permission");
        final String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this, permissions, NTUBusActivity.RC_HANDLE_ACCESS_FINE_LOCATION);
            return;
        }

        final Activity thisActivity = this;

        new AlertDialog.Builder(this).setTitle(R.string.dialog_title_request_permission_gps)
                .setMessage(R.string.dialog_message_request_permission_gps_view_map_rationale)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> ActivityCompat.requestPermissions(thisActivity, permissions, NTUBusActivity.RC_HANDLE_ACCESS_FINE_LOCATION)).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_ACCESS_FINE_LOCATION) {
            LogHelper.d(LocManager.TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            LogHelper.d(LocManager.TAG, "Location permission granted - enabling my location");
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
            return;
        }

        LogHelper.e(LocManager.TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
        Toast.makeText(this, "No Permission for current location", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (marker.getTag() == null) return;
        Object type = marker.getTag();
        LogHelper.d("NTU-BUS-MAP", "Indo clicked of " + type.getClass().toString());
        if (type instanceof BusStopJSON) {
            BusStopJSON json = (BusStopJSON) type;
            json.getBusStopCode();
            json.getDescription();

            Intent pBusIntent = new Intent(this, BusServicesAtStopRecyclerActivity.class);
            pBusIntent.putExtra("stopCode", json.getBusStopCode());
            if (json.getDescription() != null) pBusIntent.putExtra("stopName", json.getDescription());
            startActivity(pBusIntent);
        }

    }

    private final ArrayList<Marker> busMarkers = new ArrayList<>();
    private final ArrayList<Marker> publicBusMarkers = new ArrayList<>();

    private AsyncTask<String, Void, Integer> runningBus = null;
    private AsyncTask<Void, Void, Integer> runningPBus = null;

    private Handler refreshHandler;
    private boolean shouldAutoRefresh = false;
    public static final int REFRESH_TASK = 3000;
    private final Runnable refreshTask = () -> {
        LogHelper.i(TAG, "Auto-refreshing bus data at " + StaticVariables.INSTANCE.convertDateToString(new Date(System.currentTimeMillis())));
        getData(true);
    };

    /**
     * Parsing and processing the data received from the API call
     * Might also make it in a async task in the future
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String data = intent.getStringExtra("data");
            int update = intent.getIntExtra("update", 0);
            if (data == null) return;
            Gson gson = new Gson();
            NTUBus busObj;
            try {
                busObj = gson.fromJson(data, NTUBus.class);
            } catch (JsonSyntaxException e) {
                Toast.makeText(context, "An error occurred. Please try again later", Toast.LENGTH_LONG).show();
                return;
            }
            if (busObj == null) return;
            assert busObj.getRoutes() != null;
            if (busObj.getRoutes().length <= 0) return;

            if (update == 0) {
                mMap.clear();
                // Readd public bus markers if any
                if (publicBusMarkers.size() > 0) {
                    List<Marker> tmp = new ArrayList<>();
                    BitmapDescriptor bus = busesUtil.vectorToBitmapDescriptor(R.drawable.ic_bus, context, getRouteColor(199179));
                    for (Marker m : publicBusMarkers) {
                        tmp.add(mMap.addMarker(new MarkerOptions().position(m.getPosition()).title(m.getTitle()).snippet(m.getSnippet()).icon(bus)));
                    }
                    publicBusMarkers.clear();
                    publicBusMarkers.addAll(tmp);
                }
                busMarkers.clear();

                @Nullable NTUBus.MapPoints centerOn = null;
                if (busObj.getRoutes() != null) {
                    List<LatLng> mapToDraw = new ArrayList<>();
                    for (NTUBus.Route r : busObj.getRoutes()) {
                        if (r.getRoute() != null) {
                            mapToDraw.clear();
                            assert r.getRoute().getCenter() != null;
                            assert r.getRoute().getNodes() != null;
                            if (r.getRoute().getCenter().length > 0)
                                centerOn = r.getRoute().getCenter()[0];
                            BitmapDescriptor stop = busesUtil.vectorToBitmapDescriptor(R.drawable.ic_circle, context, getRouteColor(r.getId()));
                            for (NTUBus.MapNodes node : r.getRoute().getNodes()) {
                                if (node.is_stop_point()) {
                                    mMap.addMarker(new MarkerOptions().position(new LatLng(node.getLat(), node.getLon()))
                                            .title(node.getName())
                                            .snippet("Next Stop: " + node.getShort_direction())
                                            .icon(stop)).setTag(node);
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

                            LogHelper.i(TAG, "Generated " + r.getRoutename());
                        }
                    }
                }

                LatLng myLatLng;
                if (centerOn != null) myLatLng = new LatLng(centerOn.getLat(), centerOn.getLon());
                else myLatLng = new LatLng(1.3478184567642855, 103.68342014685716); // Hardcode center of school
                mMap.setOnMapLoadedCallback(() -> mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 15.4f)));
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
            }

            campusWeekend.setEnabled(true);
            campusRider.setEnabled(true);
            campusRed.setEnabled(true);
            campusBlue.setEnabled(true);
        }
    };

    private List<Marker> addBusesIntoRoute(NTUBus.Route r) {
        List<Marker> markers = new ArrayList<>();
        if (r.getVehicles() != null && r.getVehicles().length > 0) {
            BitmapDescriptor bus = busesUtil.vectorToBitmapDescriptor(R.drawable.ic_bus, this, getRouteColor(r.getId()));
            for (NTUBus.Vehicles v : r.getVehicles()) {
                // TODO: Find a way to do the bearing lol
                /*Bitmap arrow = busesUtil.vectorToBitmap(R.drawable.ic_chevron, getResources(), getRouteColor(r.getId()));
                Matrix matrix = new Matrix();
                matrix.setRotate(v.getBearing());
                arrow = Bitmap.createBitmap(arrow, 0, 0, arrow.getWidth(), arrow.getHeight(), matrix, true);*/
                markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(v.getLatVal(), v.getLonVal()))
                        .title(v.getLicense_no()).snippet("Speed: " + v.getSpeed() + " km/h | Bearing: " + v.getBearing() + " (" + getBDirection(v.getBearing()) + ")")
                        .icon(bus)));
            }
        }
        return markers;
    }

    private String getBDirection(int bearing) {
        String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        return directions[ (int)Math.round((  ((double) Math.abs(bearing) % 360) / 45)) % 8 ];
    }

    private int getRouteColor(int id) {
        switch (id) {
            case 44478: return Color.RED;
            case 44479: return Color.BLUE;
            case 44480: return Color.GREEN;
            case 44481: return Color.parseColor("#964B00");
            case 199179: return Color.parseColor("#800080"); // SBST
            default: return Color.BLACK;
        }
    }

    /**
     * Parsing and processing the data received for public API calls
     */
    private final BroadcastReceiver publicBusReceiver = new BroadcastReceiver() {
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
                BitmapDescriptor stop = busesUtil.vectorToBitmapDescriptor(R.drawable.ic_circle, context, getRouteColor(199179));
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
                    mMap.addMarker(new MarkerOptions().position(new LatLng(node.getLatitude(), node.getLongitude()))
                            .title(node.getDescription() + " (" + node.getRoadName() + ")")
                            .snippet("Bus Svcs: " + services)
                            .icon(stop)).setTag(node);
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
                    if (busObjs == null || busObjs.getServices() == null) continue;
                    if (busObjs.getServices().length <= 0) continue;

                    BusArrivalArrayObject o = busObjs.getServices()[0];
                    // Remove bus markers for this specific service
                    Iterator<Marker> iter = publicBusMarkers.iterator();
                    while (iter.hasNext()) {
                        Marker m = iter.next();

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
    };

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
            BitmapDescriptor bus = busesUtil.vectorToBitmapDescriptor(R.drawable.ic_bus, this, getRouteColor(199179));
            publicBusMarkers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(e1.getLatitudeD(), e1.getLongitudeD()))
                    .title(o.getServiceNo() + " (" + o.getOperator() + ")").snippet(load + " (" + BusesUtil.INSTANCE.getType(e1.getTypeInt()) + ")")
                    .icon(bus)));
        }
    }
}

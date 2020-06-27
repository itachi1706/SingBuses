package com.itachi1706.busarrivalsg.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.itachi1706.busarrivalsg.AsyncTasks.PopulateListWithCurrentLocationRecycler;
import com.itachi1706.busarrivalsg.Database.BusStopsDB;
import com.itachi1706.busarrivalsg.R;
import com.itachi1706.busarrivalsg.RecyclerViews.BusStopRecyclerAdapter;
import com.itachi1706.busarrivalsg.gsonObjects.sgLTA.BusStopJSON;
import com.itachi1706.busarrivalsg.util.BusesUtil;
import com.itachi1706.busarrivalsg.util.OnMapViewReadyListener;
import com.itachi1706.helperlib.helpers.LogHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Kenneth on 5/8/2018.
 * for com.itachi1706.busarrivalsg.Fragments in SingBuses
 */
public class BusStopNearbyFragment extends Fragment implements OnMapViewReadyListener.OnGlobalMapReadyListener, GoogleMap.OnInfoWindowClickListener {

    RecyclerView result;

    BusStopRecyclerAdapter adapter;
    MapView mapView;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private BusStopsDB db;
    private static final String TAG = "NearbyFrag";

    public static final String RECEIVE_LOCATION_EVENT = "ReceiveLocationEvent";
    public static final String RECEIVE_NEARBY_STOPS_EVENT = "ReceiveNearbyEvent";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v  = inflater.inflate(R.layout.fragment_bus_stops_nearby, container, false);

        if (getActivity() == null) {
            LogHelper.e(TAG, "No activity found");
            return v;
        }

        result = v.findViewById(R.id.rvNearestBusStops);
        if (result != null) result.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        result.setLayoutManager(linearLayoutManager);
        result.setItemAnimator(new DefaultItemAnimator());

        adapter = new BusStopRecyclerAdapter(new ArrayList<>());
        result.setAdapter(adapter);

        // Populate with blank
        db = new BusStopsDB(getContext());
        ArrayList<BusStopJSON> results = db.getAllBusStops();
        adapter.updateAdapter(results);
        adapter.notifyDataSetChanged();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            getActivity().getWindow().getDecorView().setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        mapView = v.findViewById(R.id.mapView);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView.onCreate(savedInstanceState);
        new OnMapViewReadyListener(mapView, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        if (getContext() != null) {
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver, new IntentFilter(RECEIVE_LOCATION_EVENT));
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(nearbyReceiver, new IntentFilter(RECEIVE_NEARBY_STOPS_EVENT));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        if (getContext() != null) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(nearbyReceiver);
        }
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = new Location("");
            location.setLatitude(intent.getDoubleExtra("lat", 0));
            location.setLongitude(intent.getDoubleExtra("lng", 0));
            if (db == null) db = new BusStopsDB(getContext());

            if (nearbyTask == null || nearbyTask.getStatus().equals(AsyncTask.Status.FINISHED))
                nearbyTask = new PopulateListWithCurrentLocationRecycler(getActivity(), db, adapter).execute(location);
        }
    };

    private static AsyncTask<Location, Void, Integer> nearbyTask = null;

    private HashMap<Marker, BusStopJSON> markerMap;

    private final BroadcastReceiver nearbyReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mMap == null) return; // Don't do anything as no map is initialized
            // If this is invoked it means that GPS permission is granted so ignore GPS permission
            if (locationManager == null) checkGpsForCurrentLocation();

            mMap.clear();
            if (markerMap == null) markerMap = new HashMap<>();
            markerMap.clear();

            String data = intent.getStringExtra("data");
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<BusStopJSON>>() {}.getType();
            ArrayList<BusStopJSON> stops = gson.fromJson(data, listType);

            for (BusStopJSON stop : stops) {
                String serviceString = stop.getServices();
                if (serviceString == null) continue;
                String[] svcsRaw = serviceString.split(",");
                StringBuilder services = new StringBuilder();
                for (String svc : svcsRaw) {
                    services.append(svc.split(":")[0]).append(", ");
                }
                markerMap.put(mMap.addMarker(new MarkerOptions().position(new LatLng(stop.getLatitude(), stop.getLongitude()))
                        .title(stop.getDescription() + " (" + stop.getRoadName() + ")")
                        .snippet("Bus Svcs: " + services.toString().replaceAll(", $", ""))
                        .icon(BusesUtil.INSTANCE.vectorToBitmapDescriptor(R.drawable.red_circle, context))), stop);
            }

            zoomToLocation();
        }
    };

    private void checkGpsForCurrentLocation() {
        if (getContext() == null) return;
        int rc = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LogHelper.d(TAG, "Google Map Ready");
        mMap = googleMap;
        mMap.setTrafficEnabled(true);
        checkGpsForCurrentLocation();
        mMap.setOnInfoWindowClickListener(this);
        UiSettings settings = mMap.getUiSettings();
        settings.setZoomControlsEnabled(true);
        settings.setMapToolbarEnabled(false);

        zoomToLocation();
    }

    private boolean isAnimating = false;

    @SuppressLint("MissingPermission")
    private void zoomToLocation() {
        if (locationManager != null && !isAnimating) {
            // Assume that location permissions are granted as only then would it be initialized
            // Zoom to current location
            isAnimating = true;
            Location myLoc = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), false));
            if (myLoc == null) return;
            LatLng myLatLng = new LatLng(myLoc.getLatitude(), myLoc.getLongitude());
            LogHelper.d(TAG, "animateCamera:onStart");
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 17), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    LogHelper.d(TAG, "animateCamera:onFinish");
                    isAnimating = false;
                }

                @Override
                public void onCancel() {
                    LogHelper.d(TAG, "animateCamera:onCancel");
                    isAnimating = false;
                }
            });
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        LogHelper.d(TAG, "Marker Info Clicked (" + marker.getTitle() + ")");
        BusStopJSON stop = markerMap.get(marker);
        if (stop == null) return;
        adapter.handleClick(getContext(), stop);
    }
}

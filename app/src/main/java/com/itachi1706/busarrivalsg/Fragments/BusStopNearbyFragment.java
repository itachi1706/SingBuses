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
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.itachi1706.busarrivalsg.AsyncTasks.PopulateListWithCurrentLocationRecycler;
import com.itachi1706.busarrivalsg.Database.BusStopsDB;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusStopJSON;
import com.itachi1706.busarrivalsg.R;
import com.itachi1706.busarrivalsg.RecyclerViews.BusStopRecyclerAdapter;

import java.util.ArrayList;

/**
 * Created by Kenneth on 5/8/2018.
 * for com.itachi1706.busarrivalsg.Fragments in SingBuses
 */
public class BusStopNearbyFragment extends Fragment implements OnMapReadyCallback {

    RecyclerView result;

    BusStopRecyclerAdapter adapter;
    MapView mapView;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private BusStopsDB db;

    public static final String RECEIVE_LOCATION_EVENT = "ReceiveLocationEvent";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v  = inflater.inflate(R.layout.fragment_bus_stops_nearby, container, false);

        if (getActivity() == null) {
            Log.e("NearbyFrag", "No activity found");
            return v;
        }

        result = v.findViewById(R.id.rvNearestBusStops);
        if (result != null) result.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
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
        mapView.getMapAsync(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        if (getContext() != null)
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver, new IntentFilter(RECEIVE_LOCATION_EVENT));
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        if (getContext() != null)
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = new Location("");
            location.setLatitude(intent.getDoubleExtra("lat", 0));
            location.setLongitude(intent.getDoubleExtra("lng", 0));
            BusStopsDB db = new BusStopsDB(getContext());
            new PopulateListWithCurrentLocationRecycler(getActivity(), db, adapter).execute(location);
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

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("NearbyFrag", "Google Map Ready");
        mMap = googleMap;
        mMap.setTrafficEnabled(true);
        checkGpsForCurrentLocation();

        if (locationManager != null) {
            // Assume that location permissions are granted as only then would it be initialized
            // Zoom to current location
            Location myLoc = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), false));
            LatLng myLatLng = new LatLng(myLoc.getLatitude(), myLoc.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 17));
        }
    }
}

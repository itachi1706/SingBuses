package com.itachi1706.busarrivalsg;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.itachi1706.busarrivalsg.Services.LocManager;
import com.itachi1706.busarrivalsg.objects.CommonEnums;
import com.itachi1706.busarrivalsg.util.BusesUtil;
import com.itachi1706.busarrivalsg.util.OnMapViewReadyListener;
import com.itachi1706.busarrivalsg.util.StaticVariables;
import com.itachi1706.helperlib.helpers.LogHelper;

public class BusLocationMapsDialogFragment extends DialogFragment implements OnMapViewReadyListener.OnGlobalMapReadyListener {

    private GoogleMap mMap;

    private double busLatitude, busLongitude;

    private double lat1, lng1, lat2, lng2, lat3, lng3;
    private String arr1, arr2, arr3;
    private int type1, type2, type3;
    private String curTime;
    private int state;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppTheme_AlertDialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_bus_location_map, container, false);

        Button close = view.findViewById(R.id.close_btn);
        close.setOnClickListener(view1 -> dismiss());

        SupportMapFragment mapFragment = new SupportMapFragment();

        getDialog().setTitle("");
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.mapView, mapFragment).commit();

        busLatitude = this.getArguments().getDouble("buslat", 0);
        busLongitude = this.getArguments().getDouble("buslng", 0);

        String bc = this.getArguments().getString("busCode", "Unknown");
        String bsn = this.getArguments().getString("busSvcNo", "Unknown");

        // 3 buses
        lat1 = this.getArguments().getDouble("lat1", 0);
        lng1 = this.getArguments().getDouble("lng1", 0);
        lat2 = this.getArguments().getDouble("lat2", 0);
        lng2 = this.getArguments().getDouble("lng2", 0);
        lat3 = this.getArguments().getDouble("lat3", 0);
        lng3 = this.getArguments().getDouble("lng3", 0);
        arr1 = this.getArguments().getString("arr1", "Unknown");
        arr2 = this.getArguments().getString("arr2", "Unknown");
        arr3 = this.getArguments().getString("arr3", "Unknown");
        type1 = this.getArguments().getInt("type1", CommonEnums.UNKNOWN);
        type2 = this.getArguments().getInt("type2", CommonEnums.UNKNOWN);
        type3 = this.getArguments().getInt("type3", CommonEnums.UNKNOWN);
        state = this.getArguments().getInt("state", StaticVariables.CUR);
        curTime = this.getArguments().getString("sTime", null);

        // Obtain the FirebaseAnalytics instance.
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this.getActivity());
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Service No: " + bsn + " | Stop Code: " + bc);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "mapDialogLaunched");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        new OnMapViewReadyListener(mapFragment, this);
        return view;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setTrafficEnabled(true);

        // Formally from layout
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        UiSettings mapSettings = mMap.getUiSettings();
        mapSettings.setCompassEnabled(true);
        mapSettings.setRotateGesturesEnabled(true);
        mapSettings.setScrollGesturesEnabled(true);
        mapSettings.setTiltGesturesEnabled(true);
        mapSettings.setZoomControlsEnabled(true);
        mapSettings.setZoomGesturesEnabled(true);

        checkIfYouHaveGpsPermissionForThis();

        // Add a marker to the bus location and move the camera
        LatLng busStopLocation = new LatLng(busLatitude, busLongitude);

        Marker m1 = null, m2 = null, m3 = null;
        LatLngBounds.Builder b = new LatLngBounds.Builder();

        // Add 3 buses location
        if (StaticVariables.INSTANCE.checkBusLocationValid(lat1, lng1)) {
            m1 = mMap.addMarker(new MarkerOptions().position(new LatLng(lat1, lng1)).title("Location of Bus 1")
                    .snippet("ETA: " + processArrival(arr1) + " (" + processType(type1) + ")")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_stop)));
            b.include(m1.getPosition());
        }
        if (StaticVariables.INSTANCE.checkBusLocationValid(lat2, lng2)) {
            m2 = mMap.addMarker(new MarkerOptions().position(new LatLng(lat2, lng2)).title("Location of Bus 2")
                    .snippet("ETA: " + processArrival(arr2) + " (" + processType(type2) + ")")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_stop)));
            if (state == StaticVariables.NEXT || state == StaticVariables.SUB)
                b.include(m2.getPosition());
        }
        if (StaticVariables.INSTANCE.checkBusLocationValid(lat3, lng3)) {
            m3 = mMap.addMarker(new MarkerOptions().position(new LatLng(lat3, lng3)).title("Location of Bus 3")
                    .snippet("ETA: " + processArrival(arr3) + " (" + processType(type3) + ")")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_stop)));
            if (state == StaticVariables.SUB) b.include(m3.getPosition());
        }
        Marker cur = BusesUtil.INSTANCE.getCurrentMarker(m1, m2, m3, state);

        Marker stop = mMap.addMarker(new MarkerOptions().position(busStopLocation).title(getString(R.string.maps_marker_bus_stop_title))
                .snippet(getString(R.string.maps_marker_bus_stop_snippet)).icon(BitmapDescriptorFactory.fromResource(R.drawable.pegman)));
        b.include(stop.getPosition());
        LatLngBounds boundary = b.build();
        mMap.setOnMapLoadedCallback(() -> {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundary, 100), 500, new GoogleMap.CancelableCallback() {
                @Override
                public void onCancel() {
                    LogHelper.i(LocManager.TAG, "Map moving animation cancelled");
                }

                @Override
                public void onFinish() {
                    LogHelper.i(LocManager.TAG, "Map moving animation finished");
                    if (cur != null) cur.showInfoWindow();
                }
            });
        });
    }

    private String processArrival(String estString) {
        long est = StaticVariables.INSTANCE.parseLTAEstimateArrival(estString,
                StaticVariables.INSTANCE.useServerTime(PreferenceManager.getDefaultSharedPreferences(getContext())), curTime);
        if (est == -9999) return "=";
        else if (est <= 0) return "Arriving";
        else if (est == 1) return est + " mins";
        else return est + " mins";
    }

    private String processType(int type) {
        switch (type) {
            case CommonEnums.BUS_BENDY:
                return "Bendy Bus";
            case CommonEnums.BUS_DOUBLE_DECK:
                return "Double Decker Bus";
            case CommonEnums.BUS_SINGLE_DECK:
                return "Normal Bus";
            case CommonEnums.UNKNOWN:
            default:
                return "Unknown Bus Type";
        }
    }

    private void checkIfYouHaveGpsPermissionForThis() {
        int rc = ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            requestGpsPermission();
        }
    }

    private void requestGpsPermission() {
        LogHelper.w(LocManager.TAG, "GPS permission is not granted. Requesting permission");
        final String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
            requestGps.launch(permissions);
            return;
        }

        new AlertDialog.Builder(this.getActivity()).setTitle(R.string.dialog_title_request_permission_gps)
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
                    new AlertDialog.Builder(getActivity()).setTitle(R.string.dialog_title_permission_denied)
                            .setMessage(R.string.dialog_message_no_permission_gps).setPositiveButton(android.R.string.ok, null)
                            .setNeutralButton(R.string.dialog_action_neutral_app_settings, (dialog, which) -> {
                                Intent permIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri packageURI = Uri.parse("package:" + requireActivity().getPackageName());
                                permIntent.setData(packageURI);
                                startActivity(permIntent);
                            }).show();
                }
            });
}

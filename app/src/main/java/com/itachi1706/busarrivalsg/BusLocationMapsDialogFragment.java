package com.itachi1706.busarrivalsg;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.analytics.FirebaseAnalytics;

public class BusLocationMapsDialogFragment extends DialogFragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    private double latitude, longitude;
    private double busLatitude, busLongitude;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_bus_location_map, container, false);

        Button close = (Button) view.findViewById(R.id.close_btn);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        SupportMapFragment mapFragment = new SupportMapFragment();

        getDialog().setTitle("");
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.mapView, mapFragment).commit();

        latitude = this.getArguments().getDouble("lat", 0);
        longitude = this.getArguments().getDouble("lng", 0);

        busLatitude = this.getArguments().getDouble("buslat", 0);
        busLongitude = this.getArguments().getDouble("buslng", 0);

        String bc = this.getArguments().getString("busCode", "Unknown");
        String bsn = this.getArguments().getString("busSvcNo", "Unknown");

        // Obtain the FirebaseAnalytics instance.
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this.getActivity());
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Service No: " + bsn + " | Stop Code: " + bc);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "mapDialogLaunched");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment.getMapAsync(this);
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
        LatLng busLocation = new LatLng(latitude, longitude);
        LatLng busStopLocation = new LatLng(busLatitude, busLongitude);

        mMap.addMarker(new MarkerOptions().position(busLocation).title(getString(R.string.maps_marker_bus_location_title))
                .snippet(getString(R.string.maps_marker_bus_location_snippet)).icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_stop)))
                .showInfoWindow();
        mMap.addMarker(new MarkerOptions().position(busStopLocation).title(getString(R.string.maps_marker_bus_stop_title))
                .snippet(getString(R.string.maps_marker_bus_stop_snippet)).icon(BitmapDescriptorFactory.fromResource(R.drawable.pegman)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(busLocation, 17));
    }

    private static final int RC_HANDLE_ACCESS_FINE_LOCATION = 3;

    private void checkIfYouHaveGpsPermissionForThis() {
        int rc = ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            requestGpsPermission(RC_HANDLE_ACCESS_FINE_LOCATION);
        }
    }

    private void requestGpsPermission(final int code) {
        Log.w("GPSManager", "GPS permission is not granted. Requesting permission");
        final String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this.getActivity(), permissions, code);
            return;
        }

        final Activity thisActivity = this.getActivity();

        new AlertDialog.Builder(this.getActivity()).setTitle(R.string.dialog_title_request_permission_gps)
                .setMessage(R.string.dialog_message_request_permission_gps_view_map_rationale)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(thisActivity, permissions, code);
                    }
                }).show();
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
        if (requestCode != RC_HANDLE_ACCESS_FINE_LOCATION) {
            Log.d("GPSManager", "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("GPSManager", "Location permission granted - enabling my location");
            // we have permission, so create the camerasource
            if (ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
            return;
        }

        Log.e("GPSManager", "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
        final Activity thisActivity = this.getActivity();
        new AlertDialog.Builder(this.getActivity()).setTitle(R.string.dialog_title_permission_denied)
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

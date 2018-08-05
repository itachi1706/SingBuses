package com.itachi1706.busarrivalsg.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
public class BusStopNearbyFragment extends Fragment {

    RecyclerView result;

    BusStopRecyclerAdapter adapter;
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
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver, new IntentFilter(RECEIVE_LOCATION_EVENT));
    }

    @Override
    public void onPause() {
        super.onPause();
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
}

package com.itachi1706.busarrivalsg.Fragments;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.itachi1706.busarrivalsg.Database.BusStopsDB;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusStopJSON;
import com.itachi1706.busarrivalsg.R;
import com.itachi1706.busarrivalsg.RecyclerViews.BusStopRecyclerAdapter;

import java.util.ArrayList;

/**
 * Created by Kenneth on 5/8/2018.
 * for com.itachi1706.busarrivalsg.Fragments in SingBuses
 */
public class BusStopSearchFragment extends Fragment {

    RecyclerView result;
    EditText textLane;

    BusStopRecyclerAdapter adapter;

    private BusStopsDB db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v  = inflater.inflate(R.layout.fragment_bus_stops_search, container, false);

        if (getActivity() == null) {
            Log.e("SearchFrag", "No activity found");
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

        textLane = v.findViewById(R.id.inputData);
        TextWatcher inputWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

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
        return v;
    }
}

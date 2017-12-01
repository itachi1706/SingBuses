package com.itachi1706.busarrivalsg;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.itachi1706.busarrivalsg.Database.BusStopsDB;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusStopJSON;
import com.itachi1706.busarrivalsg.RecyclerViews.BusStopRecyclerAdapter;

import java.util.ArrayList;

public class ListAllBusStopsActivity extends AppCompatActivity {

    TextView count;
    RecyclerView busStops;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_all_bus_stops);

        count = findViewById(R.id.tvCount);
        busStops = findViewById(R.id.rvAllBusStops);
        if (busStops != null) busStops.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        busStops.setLayoutManager(linearLayoutManager);
        busStops.setItemAnimator(new DefaultItemAnimator());

        BusStopsDB db = new BusStopsDB(this);
        ArrayList<BusStopJSON> data = db.getAllBusStops();
        count.setText(getString(R.string.label_bus_stops_count, data.size()));

        BusStopRecyclerAdapter view = new BusStopRecyclerAdapter(data);
        busStops.setAdapter(view);
    }
}

package com.itachi1706.busarrivalsg;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.itachi1706.busarrivalsg.Database.BusStopsDB;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusStopJSON;
import com.itachi1706.busarrivalsg.ListViews.BusStopListView;

import java.util.ArrayList;

public class ListAllBusStops extends AppCompatActivity {

    TextView count;
    ListView busStops;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_all_bus_stops);

        count = (TextView) findViewById(R.id.tvCount);
        busStops = (ListView) findViewById(R.id.lvAllBusStops);

        BusStopsDB db = new BusStopsDB(this);
        ArrayList<BusStopJSON> data = db.getAllBusStops();
        count.setText("Bus Stops Count: " + data.size());

        BusStopListView view = new BusStopListView(this, R.layout.listview_bus_stops, data);
        busStops.setAdapter(view);
    }
}

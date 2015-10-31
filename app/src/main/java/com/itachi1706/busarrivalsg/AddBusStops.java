package com.itachi1706.busarrivalsg;

import android.content.Intent;
import android.location.Location;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.itachi1706.busarrivalsg.AsyncTasks.PopulateListWithCurrentLocation;
import com.itachi1706.busarrivalsg.Database.BusStopsDB;
import com.itachi1706.busarrivalsg.Database.BusStopsGeoDB;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusStopJSON;
import com.itachi1706.busarrivalsg.ListViews.BusStopListView;
import com.itachi1706.busarrivalsg.Services.GPSManager;

import java.util.ArrayList;

public class AddBusStops extends AppCompatActivity {

    FloatingActionButton currentLocationGet;
    ListView result;
    EditText textLane;

    GPSManager gps;

    double longitude, latitude;

    BusStopListView adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bus_stops);

        currentLocationGet = (FloatingActionButton) findViewById(R.id.current_location_fab);
        result = (ListView) findViewById(R.id.lvNearestBusStops);

        adapter = new BusStopListView(this, R.layout.listview_bus_stops, new ArrayList<BusStopJSON>());
        result.setAdapter(adapter);

        result.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("Size", "" + result.getCount());
                BusStopJSON item = (BusStopJSON) result.getItemAtPosition(position);
                Intent serviceIntent = new Intent(AddBusStops.this, BusServicesAtStopRecyclerActivity.class);
                serviceIntent.putExtra("stopCode", item.getCode());
                serviceIntent.putExtra("stopName", item.getBusStopName());
                startActivity(serviceIntent);
            }
        });

        textLane = (EditText) findViewById(R.id.inputData);
        TextWatcher inputWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString();
                Log.d("TextWatcher", "Query searched: " + query);
                BusStopsDB db = new BusStopsDB(AddBusStops.this);
                ArrayList<BusStopJSON> results = db.getBusStopsByQuery(query);
                if (results != null) {
                    Log.d("TextWatcher", "Finished Search. Size: " + results.size());
                    adapter.updateAdapter(results);
                    adapter.notifyDataSetChanged();
                }
            }
        };
        textLane.addTextChangedListener(inputWatcher);
    }

    @Override
    public void onResume(){
        super.onResume();

        gps = new GPSManager(this);
        if (!gps.canGetLocation()){
            gps.showSettingsAlert();
        }

        currentLocationGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Getting your location...", Toast.LENGTH_SHORT).show();
                latitude = gps.getLatitude();
                longitude = gps.getLongitude();
                updateList();
            }
        });
    }

    private void updateList(){
        BusStopsGeoDB geoDB = new BusStopsGeoDB(this);
        BusStopsDB db = new BusStopsDB(this);
        Location location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        new PopulateListWithCurrentLocation(this, db, geoDB, adapter).execute(location);
    }

    @Override
    public void onPause(){
        super.onPause();
        if (gps != null)
            gps.stopUsingGPS();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_bus_stops, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, MainSettings.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

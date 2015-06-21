package com.itachi1706.busarrivalsg;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.itachi1706.busarrivalsg.AsyncTasks.GetBusServices;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusArrivalArrayObject;
import com.itachi1706.busarrivalsg.ListViews.BusServiceListViewAdapter;

import java.util.ArrayList;

public class BusServicesAtStop extends AppCompatActivity {

    ListView buses;
    String busStopCode, busStopName;
    BusServiceListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_services_at_stop);

        if (this.getIntent().hasExtra("stopCode")) busStopCode = this.getIntent().getStringExtra("stopCode");
        if (this.getIntent().hasExtra("stopName")) busStopName = this.getIntent().getStringExtra("stopName");

        buses = (ListView) findViewById(R.id.lvBusService);
        adapter = new BusServiceListViewAdapter(this, R.layout.listview_bus_numbers, new ArrayList<BusArrivalArrayObject>());
        buses.setAdapter(adapter);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onResume(){
        super.onResume();
        if (busStopCode == null){
            Log.e("BUS-SERVICE", "You aren't supposed to be here. Exiting");
            Toast.makeText(this, "Invalid Access to Activity. Exiting...", Toast.LENGTH_SHORT).show();
            this.finish();
        }else {
            if (busStopName != null)
                getSupportActionBar().setTitle(busStopName + " (" + busStopCode + ")");
            else
                getSupportActionBar().setTitle(busStopCode + "");
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            new GetBusServices(dialog, this, adapter).execute(busStopCode);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bus_services_at_stop, menu);
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

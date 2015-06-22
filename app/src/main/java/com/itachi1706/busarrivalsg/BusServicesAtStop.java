package com.itachi1706.busarrivalsg;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.itachi1706.busarrivalsg.AsyncTasks.GetBusServices;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusArrivalArrayObject;
import com.itachi1706.busarrivalsg.ListViews.BusServiceListViewAdapter;
import com.itachi1706.busarrivalsg.Objects.BusServices;
import com.itachi1706.busarrivalsg.Services.BusStorage;

import java.util.ArrayList;

public class BusServicesAtStop extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    ListView buses;
    String busStopCode, busStopName;
    BusServiceListViewAdapter adapter;
    SwipeRefreshLayout swipeToRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_services_at_stop);

        if (this.getIntent().hasExtra("stopCode")) busStopCode = this.getIntent().getStringExtra("stopCode");
        if (this.getIntent().hasExtra("stopName")) busStopName = this.getIntent().getStringExtra("stopName");

        buses = (ListView) findViewById(R.id.lvBusService);
        adapter = new BusServiceListViewAdapter(this, R.layout.listview_bus_numbers, new ArrayList<BusArrivalArrayObject>());
        buses.setAdapter(adapter);

        swipeToRefresh = (SwipeRefreshLayout) findViewById(R.id.refresh_swipe);
        swipeToRefresh.setOnRefreshListener(this);

        // TODO Swipe to refresh get 4 colors for the color scheme
        // https://github.com/itachi1706/HypixelStatistics/blob/master/app/src/main/java/com/itachi1706/hypixelstatistics/BoosterList.java for reference

        buses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BusArrivalArrayObject item = (BusArrivalArrayObject) buses.getItemAtPosition(position);
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                ArrayList<BusServices> exist = BusStorage.getStoredBuses(pref);
                boolean alrFavourited = false;
                if (exist != null){
                    //Compare if in favourites already
                    for (BusServices s : exist){
                        if (s.getServiceNo().equals(item.getServiceNo()) && s.getStopID().equals(item.getStopCode())){
                            alrFavourited = true;
                            break;
                        }
                    }
                }

                //Check based on thing and verify
                BusServices fav = new BusServices();
                fav.setObtainedNextData(false);
                fav.setOperator(item.getOperator());
                fav.setServiceNo(item.getServiceNo());
                fav.setStopID(item.getStopCode());

                addOrRemoveFav(fav, exist, pref, alrFavourited);
            }
        });
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
            swipeToRefresh.setRefreshing(true);
            updateBusStop();
        }
    }

    private void addOrRemoveFav(final BusServices fav, final ArrayList<BusServices> all, final SharedPreferences prefs, boolean alrFav){
        if (alrFav){
            new AlertDialog.Builder(this).setTitle("Remove from Favourites")
                    .setMessage("Are you sure you want to remove " + fav.getServiceNo() + " from Bus Stop Code " + fav.getStopID()
                    + " from your favourites? This will also remove it from being accessible from your Pebble device")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Remove from favourites
                            for (int i = 0; i < all.size(); i++){
                                BusServices s = all.get(i);
                                if (s.getStopID().equalsIgnoreCase(fav.getStopID()) && s.getServiceNo().equalsIgnoreCase(fav.getServiceNo())) {
                                    all.remove(i);
                                    break;
                                }
                            }
                            BusStorage.updateBusJSON(prefs, all);
                            Toast.makeText(getApplicationContext(), "Removed from favourites", Toast.LENGTH_SHORT).show();
                        }
                    }).setNegativeButton(android.R.string.no, null).show();
        } else {
            new AlertDialog.Builder(this).setTitle("Add to Favourites")
                    .setMessage("Are you sure you want to add " + fav.getServiceNo() + " from Bus Stop Code " + fav.getStopID()
                            + " to your favourites? This will also make it from being accessible from your Pebble device")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Remove from favourites
                            BusStorage.addNewBus(fav, prefs);
                            Toast.makeText(getApplicationContext(), "Added to favourites", Toast.LENGTH_SHORT).show();
                        }
                    }).setNegativeButton(android.R.string.no, null).show();
        }
    }

    private void updateBusStop(){
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        new GetBusServices(dialog, this, adapter, swipeToRefresh).execute(busStopCode);
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
        } else if (id == R.id.action_refresh){
            swipeToRefresh.setRefreshing(true);
            updateBusStop();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        updateBusStop();
    }
}

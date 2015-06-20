package com.itachi1706.busarrivalsg;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.itachi1706.busarrivalsg.AsyncTasks.GetAllBusStops;
import com.itachi1706.busarrivalsg.Database.BusStopsDB;

import java.util.UUID;

public class MainMenu extends AppCompatActivity {

    //Pebble stuff
    private PebbleKit.PebbleDataReceiver mReceiver;
    private final static UUID PEBBLE_APP_UUID = UUID.fromString("11198668-4e27-4e94-b51c-a27a1ea5cd82");

    //Android Stuff
    private TextView connectionStatus, pressedBtn;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        connectionStatus = (TextView) findViewById(R.id.pebbleConnectionStatus);
        pressedBtn = (TextView) findViewById(R.id.pressedBtn);
        fab = (FloatingActionButton) findViewById(R.id.add_fab);
    }

    @Override
    public void onResume(){
        super.onResume();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "FAB Clicked!", Toast.LENGTH_SHORT).show();
            }
        });

        boolean checkIfPebbleConnected = PebbleKit.isWatchConnected(this);
        if (checkIfPebbleConnected){
            //Init
            connectionStatus.setText("Pebble Connected!");
            connectionStatus.setTextColor(Color.GREEN);
            mReceiver = new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {
                @Override
                public void receiveData(Context context, int i, PebbleDictionary pebbleDictionary) {
                    PebbleKit.sendAckToPebble(getApplicationContext(), i);

                    //Handle stuff in the dictionary
                    if (pebbleDictionary.contains(PebbleEnum.KEY_BUTTON_EVENT)){
                        switch (pebbleDictionary.getUnsignedIntegerAsLong(PebbleEnum.KEY_BUTTON_EVENT).intValue()){
                            case PebbleEnum.BUTTON_REFRESH: pressedBtn.setText("Refresh Pressed"); break;
                            case PebbleEnum.BUTTON_NEXT: pressedBtn.setText("Going Next"); break;
                            case PebbleEnum.BUTTON_PREVIOUS: pressedBtn.setText("Going Previous"); break;
                        }
                    }
                }
            };
            PebbleKit.registerReceivedDataHandler(this, mReceiver);
        } else {
            connectionStatus.setText("Pebble not connected!");
            connectionStatus.setTextColor(Color.RED);
        }

        //Android Stuff now again :D
        checkIfDatabaseUpdated();
    }

    @Override
    public void onPause(){
        super.onPause();
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_menu, menu);
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
            return true;
        } else if (id == R.id.view_all_stops){
            startActivity(new Intent(this, ListAllBusStops.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkIfDatabaseUpdated(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp.getBoolean("firstBoot", true)){
            //First Boot, populate database
            if (!isNetworkAvailable()){
                new AlertDialog.Builder(this).setTitle("No Internet Access")
                        .setMessage("Internet Access is required for first boot, please ensure that you have internet access" +
                                " before relaunching this application").setCancelable(false)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MainMenu.this.finish();
                            }
                        }).show();
            } else {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle("First Boot");
                dialog.setMessage("Retriving data from database");
                dialog.setCancelable(false);
                dialog.setIndeterminate(true);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

                BusStopsDB db = new BusStopsDB(this);
                db.dropAndRebuildDB();
                dialog.show();

                new GetAllBusStops(dialog, db, this, sp).execute(0);
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

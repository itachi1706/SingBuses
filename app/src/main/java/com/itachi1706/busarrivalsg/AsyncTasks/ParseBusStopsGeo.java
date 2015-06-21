package com.itachi1706.busarrivalsg.AsyncTasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.itachi1706.busarrivalsg.Database.BusStopsGeoDB;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusStopsGeo;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusStopsGeoObject;

/**
 * Created by Kenneth on 20/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.AsyncTasks
 */
public class ParseBusStopsGeo extends AsyncTask<BusStopsGeo, String, Void> {

    private ProgressDialog dialog;
    private BusStopsGeoDB db;
    private Activity activity;
    private SharedPreferences sp;

    public ParseBusStopsGeo(ProgressDialog dialog, BusStopsGeoDB db, Activity activity, SharedPreferences sp){
        this.dialog = dialog;
        this.db = db;
        this.activity = activity;
        this.sp = sp;
    }

    @Override
    protected Void doInBackground(BusStopsGeo... array) {
        BusStopsGeo stops = array[0];

        BusStopsGeoObject[] busStops = stops.getGeo();
        publishProgress("start");
        for (int i = 0; i < busStops.length; i++) {
            BusStopsGeoObject busStop = busStops[i];
            publishProgress("check", i + "", busStops.length + "", busStop.getNo(), busStop.getName());
            db.addToDBIfNotExist(busStop);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... data){
        String check = data[0];
        if (check.equalsIgnoreCase("start")) {
            dialog.setTitle("Parsing JSON Data...");
            dialog.setMessage("Parsing bus stops geographical data retrived...");
        } else if (check.equalsIgnoreCase("process")) {
            String i = data[1];
            String length = data[2];
            String code = data[3];
            String name = data[4];
            Log.d("GET-STOPSGEO", "Importing " + (i + 1) + "/" + length + " (" + code + ")");
            dialog.setMessage("(" + (i + 1) + "/" + length + ") Parsing " + code + " \n[" + name + "]");
        }
    }

    protected void onPostExecute(Void param){
        //Done
        int count = db.getSize();
        Toast.makeText(activity, count + " bus stops geo data saved to database!", Toast.LENGTH_SHORT).show();
        Log.d("GET-STOPSGEO", "Loaded " + count + " bus stops geographical data into the database");
        sp.edit().putBoolean("geoDBLoaded", true).apply();
        dialog.dismiss();
    }
}

package com.itachi1706.busarrivalsg.AsyncTasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.itachi1706.busarrivalsg.Database.BusStopsDB;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusStopJSON;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusStopJSONArray;
import com.itachi1706.busarrivalsg.StaticVariables;

/**
 * Created by Kenneth on 20/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.AsyncTasks
 */
public class ParseBusStops extends AsyncTask<BusStopJSONArray, String, Void> {

    private ProgressDialog progressDialog;
    private BusStopsDB db;
    private Activity activity;
    private SharedPreferences sp;

    public ParseBusStops(ProgressDialog progressDialog, BusStopsDB db, Activity activity, SharedPreferences sp){
        this.progressDialog = progressDialog;
        this.db = db;
        this.activity = activity;
        this.sp = sp;
    }

    @Override
    protected Void doInBackground(BusStopJSONArray... array) {
        BusStopJSONArray stops = array[0];

        BusStopJSON[] busStops = stops.getBusStopsArray();
        for (int i = 0; i < busStops.length; i++) {
            BusStopJSON busStop = busStops[i];
            publishProgress((i + 1) + "", busStops.length + "", busStop.getCode(), busStop.getRoad() + " - " + busStop.getBusStopName());
            db.addToDBIfNotExist(busStop);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        int i = Integer.parseInt(values[0]);
        int totalLength = Integer.parseInt(values[1]);
        String code = values[2];
        String busStopName = values[3];
        Log.d("GET-STOPS", "Importing " + i + "/" + totalLength + " (" + code + ")");
        progressDialog.setProgress(i);
        progressDialog.setMax(totalLength);
        progressDialog.setIndeterminate(false);
        progressDialog.setMessage("Parsing bus stops data...\nCurrent Stop: " + code + "\n" + busStopName);
    }

    protected void onPostExecute(Void params){
        int count = db.getSize();
        Toast.makeText(activity, count + " bus stops saved to database!", Toast.LENGTH_SHORT).show();
        Log.d("GET-STOPS", "Loaded " + count + " bus stops into the database");
        sp.edit().putBoolean("busDBLoaded", true).apply();
        sp.edit().putLong("busDBTimeUpdated", System.currentTimeMillis()).apply();
        progressDialog.dismiss();
        StaticVariables.init1TaskFinished = true;
        Log.d("INIT-1", "Task Complete");
    }
}

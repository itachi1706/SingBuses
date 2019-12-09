package com.itachi1706.busarrivalsg.AsyncTasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.itachi1706.busarrivalsg.Database.BusStopsDB;
import com.itachi1706.busarrivalsg.gsonObjects.sgLTA.BusStopJSON;
import com.itachi1706.busarrivalsg.gsonObjects.sgLTA.BusStopJSONArray;
import com.itachi1706.busarrivalsg.R;
import com.itachi1706.busarrivalsg.util.Timings;

import java.lang.ref.WeakReference;

/**
 * Created by Kenneth on 20/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.AsyncTasks
 */
public class ParseBusStops extends AsyncTask<BusStopJSONArray, String, Void> {

    private ProgressDialog progressDialog;
    private BusStopsDB db;
    private WeakReference<Activity> actRef;
    private SharedPreferences sp;

    public ParseBusStops(ProgressDialog progressDialog, BusStopsDB db, Activity activity, SharedPreferences sp){
        this.progressDialog = progressDialog;
        this.db = db;
        this.actRef = new WeakReference<>(activity);
        this.sp = sp;
    }

    @Override
    protected Void doInBackground(BusStopJSONArray... array) {
        BusStopJSONArray stops = array[0];
        Timings t = new Timings("PBSTimeDrop", true);
        t.start();
        // Drops database
        db.dropAndRebuildDB();
        t.end();

        Timings t2 = new Timings("PBSTimeAdd", true);
        t2.start();
        BusStopJSON[] busStops = stops.getValue();
        db.addMultipleToDB(busStops);
        t2.end();
        return null;
    }

    protected void onPostExecute(Void params){
        @Nullable Activity activity = actRef.get();
        int count = db.getSize();
        if (activity != null) Toast.makeText(activity, activity.getString(R.string.toast_bus_stop_data_parse_success, count), Toast.LENGTH_SHORT).show();
        Log.d("GET-STOPS", "Loaded " + count + " bus stops into the database");
        sp.edit().putBoolean("busDBLoaded", true).apply();
        sp.edit().putLong("busDBTimeUpdated", System.currentTimeMillis()).apply();
        progressDialog.dismiss();
        Log.d("INIT-1", "Task Complete");
    }
}

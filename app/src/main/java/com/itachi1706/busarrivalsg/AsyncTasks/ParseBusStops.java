package com.itachi1706.busarrivalsg.AsyncTasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.itachi1706.busarrivalsg.Database.BusStopsDB;
import com.itachi1706.busarrivalsg.R;
import com.itachi1706.busarrivalsg.gsonObjects.sgLTA.BusStopJSON;
import com.itachi1706.busarrivalsg.gsonObjects.sgLTA.BusStopJSONArray;
import com.itachi1706.busarrivalsg.util.Timings;
import com.itachi1706.helperlib.concurrent.CoroutineAsyncTask;
import com.itachi1706.helperlib.helpers.LogHelper;

import java.lang.ref.WeakReference;

/**
 * Created by Kenneth on 20/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.AsyncTasks
 */
public class ParseBusStops extends CoroutineAsyncTask<BusStopJSONArray, String, Void> {

    private final ProgressDialog progressDialog;
    private final BusStopsDB db;
    private final WeakReference<Activity> actRef;
    private final SharedPreferences sp;
    private static final String TASK_NAME = ParseBusStops.class.getSimpleName();

    public ParseBusStops(ProgressDialog progressDialog, BusStopsDB db, Activity activity, SharedPreferences sp){
        super(TASK_NAME);
        this.progressDialog = progressDialog;
        this.db = db;
        this.actRef = new WeakReference<>(activity);
        this.sp = sp;
    }

    @Override
    public Void doInBackground(BusStopJSONArray... array) {
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

    public void onPostExecute(Void params){
        @Nullable Activity activity = actRef.get();
        int count = db.getSize();
        if (activity != null) Toast.makeText(activity, activity.getString(R.string.toast_bus_stop_data_parse_success, count), Toast.LENGTH_SHORT).show();
        LogHelper.d("GET-STOPS", "Loaded " + count + " bus stops into the database");
        sp.edit().putBoolean("busDBLoaded", true).apply();
        sp.edit().putLong("busDBTimeUpdated", System.currentTimeMillis()).apply();
        progressDialog.dismiss();
        LogHelper.d("INIT-1", "Task Complete");
    }
}

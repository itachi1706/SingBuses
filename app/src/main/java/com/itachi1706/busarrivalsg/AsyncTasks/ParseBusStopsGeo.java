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
import com.itachi1706.busarrivalsg.R;

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

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.setTitle(activity.getString(R.string.progress_title_bus_stop_geo_data_parse));
                dialog.setMessage(activity.getString(R.string.progress_message_bus_stop_geo_data_parse_pre));
            }
        });

        BusStopsGeoObject[] busStops = stops.getGeo();
        for (int i = 0; i < busStops.length; i++) {
            BusStopsGeoObject busStop = busStops[i];
            publishProgress(i + "", busStops.length + "", busStop.getNo(), busStop.getName());
            db.addToDBIfNotExist(busStop);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... data) {
        int i = Integer.parseInt(data[0]);
        int length = Integer.parseInt(data[1]);
        String code = data[2];
        String name = data[3];
        dialog.setMax(length);
        dialog.setProgress(i + 1);
        dialog.setIndeterminate(false);
        Log.d("GET-STOPSGEO", "Importing " + (i + 1) + "/" + length + " (" + code + ")");
        dialog.setMessage(activity.getString(R.string.dialog_message_bus_stop_geo_data_parse, code, name));
    }

    protected void onPostExecute(Void param){
        //Done
        int count = db.getSize();
        Toast.makeText(activity, activity.getString(R.string.toast_bus_stop_geo_data_parse_success, count), Toast.LENGTH_SHORT).show();
        Log.d("GET-STOPSGEO", "Loaded " + count + " bus stops geographical data into the database");
        sp.edit().putBoolean("geoDBLoaded", true).apply();
        sp.edit().putLong("geoDBTimeUpdated", System.currentTimeMillis()).apply();
        dialog.dismiss();
    }
}

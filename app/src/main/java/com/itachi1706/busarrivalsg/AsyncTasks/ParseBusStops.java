package com.itachi1706.busarrivalsg.AsyncTasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.itachi1706.busarrivalsg.Database.BusStopsDB;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusStopJSON;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusStopJSONArray;

/**
 * Created by Kenneth on 20/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.AsyncTasks
 */
public class ParseBusStops extends AsyncTask<BusStopJSONArray, Void, Void> {

    private ProgressDialog dialog;
    private BusStopsDB db;
    private Activity activity;
    private SharedPreferences sp;

    private int skipValue;

    public ParseBusStops(ProgressDialog dialog, BusStopsDB db, Activity activity, int skipValue, SharedPreferences sp){
        this.dialog = dialog;
        this.db = db;
        this.activity = activity;
        this.skipValue = skipValue;
        this.sp = sp;
    }

    @Override
    protected Void doInBackground(BusStopJSONArray... array) {
        BusStopJSONArray stops = array[0];

        BusStopJSON[] busStops = stops.getBusStopsArray();
        //dialog.setTitle("Parsing JSON Data...");
        //dialog.setMessage("Parsing bus stops data retrived...");
        for (int i = 0; i < busStops.length; i++) {
            BusStopJSON busStop = busStops[i];
            Log.d("GET-STOPS", "Importing " + (skipValue + i + 1) + "/" + (skipValue + 50) + " (" + busStop.getCode() + ")");
            //dialog.setMessage("(" + (i+1) + "/" + busStops.length + ") Parsing " + busStop.getCode()
            //+ " \n[" + busStop.getBusStopName() + "]");
            db.addToDBIfNotExist(busStop);
        }
        return null;
    }

    protected void onPostExecute(Void param){
        //Do next 50
        skipValue += 50;
        new GetAllBusStops(dialog, db, activity, sp).execute(skipValue);
    }
}

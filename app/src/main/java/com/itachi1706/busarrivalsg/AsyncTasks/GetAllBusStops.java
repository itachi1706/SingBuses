package com.itachi1706.busarrivalsg.AsyncTasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.itachi1706.busarrivalsg.Database.BusStopsDB;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusStopJSONArray;
import com.itachi1706.busarrivalsg.StaticVariables;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Created by Kenneth on 20/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.AsyncTasks
 */
public class GetAllBusStops extends AsyncTask<Integer, Void, String> {

    private ProgressDialog dialog;
    private BusStopsDB db;
    private Activity activity;
    private Exception exception = null;
    private SharedPreferences sp;

    private int skipValue;

    public GetAllBusStops(ProgressDialog dialog, BusStopsDB db, Activity activity, SharedPreferences sp){
        this.dialog = dialog;
        this.db = db;
        this.activity = activity;
        this.sp = sp;
    }

    @Override
    protected String doInBackground(Integer... skipValues) {
        this.skipValue = skipValues[0];
        String url = "http://api.itachi1706.com/api/busstops.php?skip=" + this.skipValue;
        String tmp = "";

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.setTitle("Downloading Bus Stop Data");
                dialog.setMessage("This will take a few minutes. Be patient :) \nGetting Bus Stops " + (skipValue + 1) + " - " + (skipValue + 50));
            }
        });
        try {
            URL urlConn = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlConn.openConnection();
            conn.setConnectTimeout(StaticVariables.HTTP_QUERY_TIMEOUT);
            conn.setReadTimeout(StaticVariables.HTTP_QUERY_TIMEOUT);
            InputStream in = conn.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder str = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null)
            {
                str.append(line);
            }
            in.close();
            tmp = str.toString();
        } catch (IOException e) {
            exception = e;
        }
        return tmp;
    }

    protected void onPostExecute(String json){
        if (exception != null){
            if (exception instanceof SocketTimeoutException) {
                Toast.makeText(activity, "Database query timed out. Retrying", Toast.LENGTH_SHORT).show();
                new GetAllBusStops(dialog, db, activity, sp).execute(skipValue);
            } else {
                Toast.makeText(activity, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            //Go parse it
            Gson gson = new Gson();
            if (!StaticVariables.checkIfYouGotJsonString(json)){
                //Invalid string, retrying
                Toast.makeText(activity, "Invalid JSON, Retrying", Toast.LENGTH_SHORT).show();
                new GetAllBusStops(dialog, db, activity, sp).execute(skipValue);
                return;
            }

            BusStopJSONArray replyArr = gson.fromJson(json, BusStopJSONArray.class);
            if (replyArr == null || replyArr.getBusStopsArray() == null){
                //Invalid string, retrying
                Toast.makeText(activity, "Something weird occurred, Retrying", Toast.LENGTH_SHORT).show();
                new GetAllBusStops(dialog, db, activity, sp).execute(skipValue);
                return;
            }
            if (replyArr.getBusStopsArray().length == 0){
                //End of line can safely return
                int count = db.getSize();
                Toast.makeText(activity, count + " bus stops saved to database!", Toast.LENGTH_SHORT).show();
                Log.d("GET-STOPS", "Loaded " + count + " bus stops into the database");
                sp.edit().putBoolean("busDBLoaded", true).apply();
                dialog.dismiss();
                StaticVariables.init1TaskFinished = true;
                Log.d("INIT-1", "Task Complete");
                return;
            }
            new ParseBusStops(dialog, db, activity, skipValue, sp).execute(replyArr);
        }
    }
}

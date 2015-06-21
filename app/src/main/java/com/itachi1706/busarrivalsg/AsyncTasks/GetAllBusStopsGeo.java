package com.itachi1706.busarrivalsg.AsyncTasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.itachi1706.busarrivalsg.Database.BusStopsGeoDB;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusStopsGeo;
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
public class GetAllBusStopsGeo extends AsyncTask<Void, Void, String> {

    private ProgressDialog dialog;
    private BusStopsGeoDB db;
    private Activity activity;
    private Exception exception = null;
    private SharedPreferences sp;

    public GetAllBusStopsGeo(ProgressDialog dialog, BusStopsGeoDB db, Activity activity, SharedPreferences sp){
        this.dialog = dialog;
        this.db = db;
        this.activity = activity;
        this.sp = sp;
    }

    @Override
    protected String doInBackground(Void... params) {
        String url = "http://api.itachi1706.com/api/busstopsgeo.php";
        String tmp = "";

        Log.d("INIT-2 GEO", "Awaiting completion of First Init Task...");
        while (!StaticVariables.init1TaskFinished)
        {
            try
            {
                Log.d("INIT-2 GEO", "Waiting for First Init Task to Finish");
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        Log.d("INIT-2 GEO", "First Init Task Finished");
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.show();
                dialog.setTitle("Downloading Bus Stop Geographical Data");
                dialog.setMessage("This will take a few minutes. Be patient :) \nGetting Bus Stops Geographical Data...");
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
                new GetAllBusStopsGeo(dialog, db, activity, sp).execute();
            } else {
                Toast.makeText(activity, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            //Go parse it
            Gson gson = new Gson();
            if (!StaticVariables.checkIfYouGotJsonString(json)){
                //Invalid string, retrying
                Toast.makeText(activity, "Invalid JSON, Retrying", Toast.LENGTH_SHORT).show();
                new GetAllBusStopsGeo(dialog, db, activity, sp).execute();
                return;
            }

            BusStopsGeo replyArr = gson.fromJson(json, BusStopsGeo.class);
            if (replyArr.getGeo().length == 0){
                //Error occured
                Toast.makeText(activity, "An error occured, please reload the application and try again", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return;
            }
            new ParseBusStopsGeo(dialog, db, activity, sp).execute(replyArr);
        }
    }
}

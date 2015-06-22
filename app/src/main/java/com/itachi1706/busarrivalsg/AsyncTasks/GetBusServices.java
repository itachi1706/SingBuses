package com.itachi1706.busarrivalsg.AsyncTasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.itachi1706.busarrivalsg.Database.BusStopsDB;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusArrivalArrayObject;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusArrivalMain;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusStopJSONArray;
import com.itachi1706.busarrivalsg.ListViews.BusServiceListViewAdapter;
import com.itachi1706.busarrivalsg.StaticVariables;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Kenneth on 20/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.AsyncTasks
 */
public class GetBusServices extends AsyncTask<String, Void, String> {

    private ProgressDialog dialog;
    private Activity activity;
    private Exception exception = null;
    private BusServiceListViewAdapter adapter;
    private SwipeRefreshLayout swipe;

    private String busCode;

    public GetBusServices(ProgressDialog dialog, Activity activity, BusServiceListViewAdapter adapter, SwipeRefreshLayout swipe){
        this.dialog = dialog;
        this.activity = activity;
        this.adapter = adapter;
        this.swipe = swipe;
    }

    @Override
    protected String doInBackground(String... busCodes) {
        this.busCode = busCodes[0];
        String url = "http://api.itachi1706.com/api/busarrival.php?BusStopID=" + this.busCode;
        String tmp = "";

        Log.d("GET-BUS-SERVICE", url);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.setTitle("Downloading Bus Service Data");
                dialog.setMessage("Getting all Bus Services in Bus Stop " + busCode);
                dialog.show();
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
                Toast.makeText(activity, "Request Timed Out", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        } else {
            //Go parse it
            Gson gson = new Gson();
            if (!StaticVariables.checkIfYouGotJsonString(json)){
                //Invalid string, retrying
                Toast.makeText(activity, "Invalid JSON String", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return;
            }

            ArrayList<BusArrivalArrayObject> items = new ArrayList<>();
            BusArrivalMain mainArr = gson.fromJson(json, BusArrivalMain.class);
            BusArrivalArrayObject[] array = mainArr.getServices();
            String stopID = mainArr.getBusStopID();
            dialog.dismiss();
            if (swipe.isRefreshing())
                swipe.setRefreshing(false);
            for (BusArrivalArrayObject obj : array){
                obj.setStopCode(stopID);
                items.add(obj);
                adapter.updateAdapter(items);
                adapter.notifyDataSetChanged();
            }

        }
    }
}

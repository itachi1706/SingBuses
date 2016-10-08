package com.itachi1706.busarrivalsg.AsyncTasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.gson.Gson;
import com.itachi1706.busarrivalsg.Database.BusStopsDB;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusStopJSONArray;
import com.itachi1706.busarrivalsg.R;
import com.itachi1706.busarrivalsg.Util.StaticVariables;

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

    private ProgressDialog progressDialog;
    private BusStopsDB db;
    private Activity activity;
    private Exception exception = null;
    private SharedPreferences sp;

    public GetAllBusStops(ProgressDialog progressDialog, BusStopsDB db, Activity activity, SharedPreferences sp){
        this.progressDialog = progressDialog;
        this.db = db;
        this.activity = activity;
        this.sp = sp;
    }

    @Override
    protected String doInBackground(Integer... skipValues) {
        String url = "http://api.itachi1706.com/api/busstops.php?api=2";
        String tmp = "";

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.setTitle(activity.getString(R.string.progress_title_bus_stop_data_download));
                progressDialog.setMessage(activity.getString(R.string.progress_message_bus_stop_data_download));
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
                Toast.makeText(activity, R.string.toast_message_timeout_database_query_retry, Toast.LENGTH_SHORT).show();
                new GetAllBusStops(progressDialog, db, activity, sp).execute();
            } else {
                Toast.makeText(activity, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            //Go parse it
            Gson gson = new Gson();
            if (!StaticVariables.checkIfYouGotJsonString(json)){
                //Invalid string, retrying
                Toast.makeText(activity, R.string.toast_message_invalid_json_retry, Toast.LENGTH_SHORT).show();
                new GetAllBusStops(progressDialog, db, activity, sp).execute();
                return;
            }
            BusStopJSONArray replyArr = gson.fromJson(json, BusStopJSONArray.class);
            if (replyArr == null || replyArr.getBusStopsArray() == null){
                //Invalid string, retrying
                Toast.makeText(activity, R.string.toast_message_unknown_error, Toast.LENGTH_SHORT).show();
                new GetAllBusStops(progressDialog, db, activity, sp).execute();
                return;
            }
            progressDialog.setTitle(activity.getString(R.string.progress_title_bus_stop_data_parse_pre));
            progressDialog.setMessage(activity.getString(R.string.progress_message_bus_stop_data_parse_pre));
            new ParseBusStops(progressDialog, db, activity, sp).execute(replyArr);
        }
    }
}

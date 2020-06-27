package com.itachi1706.busarrivalsg.AsyncTasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.itachi1706.busarrivalsg.Database.BusStopsDB;
import com.itachi1706.busarrivalsg.R;
import com.itachi1706.busarrivalsg.gsonObjects.sgLTA.BusStopJSONArray;
import com.itachi1706.busarrivalsg.util.StaticVariables;
import com.itachi1706.helperlib.helpers.URLHelper;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;

/**
 * Created by Kenneth on 20/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.AsyncTasks
 */
public class GetAllBusStops extends AsyncTask<Integer, Void, String> {

    private final ProgressDialog progressDialog;
    private final BusStopsDB db;
    private final WeakReference<Activity> actRef;
    private Exception exception = null;
    private final SharedPreferences sp;

    public GetAllBusStops(ProgressDialog progressDialog, BusStopsDB db, Activity activity, SharedPreferences sp){
        this.progressDialog = progressDialog;
        this.db = db;
        this.actRef = new WeakReference<>(activity);
        this.sp = sp;
    }

    @Override
    protected String doInBackground(Integer... skipValues) {
        Activity activity = actRef.get();
        if (activity == null) return null;
        String url = "https://api.itachi1706.com/api/busstops.php?api=2";
        String tmp = "";
        URLHelper urlHelper = new URLHelper(url);

        activity.runOnUiThread(() -> {
            progressDialog.setTitle(activity.getString(R.string.progress_title_bus_stop_data_download));
            progressDialog.setMessage(activity.getString(R.string.progress_message_bus_stop_data_download));
        });
        try {
            tmp = urlHelper.executeString();
        } catch (IOException e) {
            exception = e;
        }
        return tmp;
    }

    protected void onPostExecute(@Nullable String json){
        Activity activity = actRef.get();
        if (activity == null || json == null) return;
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
            if (!StaticVariables.INSTANCE.checkIfYouGotJsonString(json)){
                //Invalid string, retrying
                Toast.makeText(activity, R.string.toast_message_invalid_json_retry, Toast.LENGTH_SHORT).show();
                new GetAllBusStops(progressDialog, db, activity, sp).execute();
                return;
            }
            BusStopJSONArray replyArr = gson.fromJson(json, BusStopJSONArray.class);
            if (replyArr == null || replyArr.getValue() == null){
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

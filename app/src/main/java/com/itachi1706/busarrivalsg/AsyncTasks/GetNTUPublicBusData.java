package com.itachi1706.busarrivalsg.AsyncTasks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.itachi1706.appupdater.Util.URLHelper;
import com.itachi1706.busarrivalsg.Database.BusStopsDB;
import com.itachi1706.busarrivalsg.NTUBusActivity;
import com.itachi1706.busarrivalsg.R;
import com.itachi1706.busarrivalsg.gsonObjects.sgLTA.BusStopJSON;
import com.itachi1706.busarrivalsg.util.StaticVariables;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/**
 * Created by Kenneth on 07/9/2018
 * for SingBuses in package com.itachi1706.busarrivalsg.AsyncTasks
 */
public class GetNTUPublicBusData extends AsyncTask<Void, Void, Integer> {

    private WeakReference<Activity> activityRef;
    private Exception except;
    private boolean update;
    private static final String TAG = "NTUPublicBusData";

    // Bus Stop Codes to get (we will get it based off the last stop on campus)
    // 199: 27199, 179(A): 27261;

    public GetNTUPublicBusData(Activity activity, boolean update) {
        this.activityRef = new WeakReference<>(activity);
        this.update = update;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        Activity mActivity = activityRef.get();
        String url = "https://api.itachi1706.com/api/busarrival.php?CSV=27199:199;27261:179;27261:179A&api=2";

        Log.d(TAG, url);
        String tmp;
        try {
            long start = System.currentTimeMillis();
            URLHelper urlHelper = new URLHelper(url);
            tmp = urlHelper.executeString();
            Log.i(TAG, "Data retrieved in " + (System.currentTimeMillis() - start) + "ms");
        } catch (IOException e) {
            except = e;
            return 1;
        }

        Log.d(TAG, tmp);
        if (!StaticVariables.INSTANCE.checkIfYouGotJsonString(tmp)) {
            except = new Exception(mActivity.getResources().getString(R.string.toast_message_invalid_json));
            return 2;
        }

        Intent sendForMapParsingIntent = new Intent(NTUBusActivity.RECEIVE_NTU_PUBLIC_BUS_DATA_EVENT);
        sendForMapParsingIntent.putExtra("data", tmp);
        sendForMapParsingIntent.putExtra("update", true);
        mActivity.runOnUiThread(() -> LocalBroadcastManager.getInstance(mActivity).sendBroadcast(sendForMapParsingIntent));

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mActivity);
        if (!update && sp.getBoolean("showntusbsstops", true)) {
            // We will send data related to stops as well
            BusStopsDB db = new BusStopsDB(mActivity);
            ArrayList<BusStopJSON> jsons = new ArrayList<>();
            jsons.addAll(db.getBusStopsBySvcNo("179", "SBST"));
            jsons.addAll(db.getBusStopsBySvcNo("179A", "SBST"));
            jsons.addAll(db.getBusStopsBySvcNo("199", "SBST"));
            BusStopJSON[] stops = jsons.toArray(new BusStopJSON[0]);

            Gson gson = new Gson();
            String js = gson.toJson(stops, BusStopJSON[].class);
            Intent sendForParseIntent = new Intent(NTUBusActivity.RECEIVE_NTU_PUBLIC_BUS_DATA_EVENT);
            sendForParseIntent.putExtra("data", js);
            sendForParseIntent.putExtra("update", false);
            mActivity.runOnUiThread(() -> LocalBroadcastManager.getInstance(mActivity).sendBroadcast(sendForParseIntent));
        }
        return 0;
    }

    protected void onPostExecute(Integer errorCode) {
        Context context = activityRef.get();
        if (except != null && errorCode != 0) {
            Log.e(TAG, "Exception occurred (" + except.getMessage() + ")");
            if (except instanceof SocketTimeoutException) {
                Toast.makeText(context, "NTU API did not respond in a timely manner", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, except.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}

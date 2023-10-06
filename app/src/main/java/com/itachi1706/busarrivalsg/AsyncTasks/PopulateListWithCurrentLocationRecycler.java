package com.itachi1706.busarrivalsg.AsyncTasks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.itachi1706.busarrivalsg.Database.BusStopsDB;
import com.itachi1706.busarrivalsg.Fragments.BusStopNearbyFragment;
import com.itachi1706.busarrivalsg.R;
import com.itachi1706.busarrivalsg.RecyclerViews.BusStopRecyclerAdapter;
import com.itachi1706.busarrivalsg.gsonObjects.Distance;
import com.itachi1706.busarrivalsg.gsonObjects.sgLTA.BusStopJSON;
import com.itachi1706.busarrivalsg.util.StaticVariables;
import com.itachi1706.helperlib.concurrent.CoroutineAsyncTask;
import com.itachi1706.helperlib.helpers.LogHelper;
import com.itachi1706.helperlib.helpers.URLHelper;
import com.itachi1706.helperlib.helpers.ValidationHelper;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/**
 * Created by Kenneth on 20/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.AsyncTasks
 */
public class PopulateListWithCurrentLocationRecycler extends CoroutineAsyncTask<Location, Void, Integer> {

    private final WeakReference<Activity> contextRef;
    private final BusStopsDB db;
    private final BusStopRecyclerAdapter adapter;
    private Exception except;
    private static final String TASK_NAME = PopulateListWithCurrentLocationRecycler.class.getSimpleName();

    private static final String TAG = "CURRENT-LOCATION";

    public PopulateListWithCurrentLocationRecycler(Activity context, BusStopsDB db, BusStopRecyclerAdapter adapter) {
        super(TASK_NAME);
        this.contextRef = new WeakReference<>(context);
        this.db = db;
        this.adapter = adapter;
    }

    @Override
    public Integer doInBackground(Location... locate) {
        Location location = locate[0];
        Activity context = contextRef.get();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int limit = Integer.parseInt(sp.getString("nearbyStopsCount", "20"));
        // Get validation stuff
        String signature = ValidationHelper.getSignatureForValidation(context);
        String url = "https://api.itachi1706.com/api/mobile/nearestBusStop.php?location=" + location.getLatitude() + "," + location.getLongitude() + "&limit=" + limit;
        LogHelper.d(TAG, url); // Don't print the signature out
        url += "&sig=" + signature + "&package=" + context.getPackageName();
        String tmp;
        try {
            URLHelper urlHelper = new URLHelper(url);
            tmp = urlHelper.executeString();
        } catch (IOException e) {
            except = e;
            return 1;
        }

        // Do the processing here
        Gson gson = new Gson();
        LogHelper.d(TAG, (tmp == null) ? "null" : tmp);
        if (tmp == null || !StaticVariables.INSTANCE.checkIfYouGotJsonString(tmp)) {
            except = new Exception(context.getResources().getString(R.string.toast_message_invalid_json));
            return 2;
        }
        Distance distArray = gson.fromJson(tmp, Distance.class);
        if (distArray == null || distArray.getResults() == null) {
            except = new Exception("Invalid Distance retrieved from API. Please try again later");
            return 3;
        }
        Distance.DistanceItem[] results = distArray.getResults();
        ArrayList<BusStopJSON> stops = new ArrayList<>();
        assert results != null;
        for (Distance.DistanceItem map : results) {
            float distance = map.getDist();
            BusStopJSON stop = db.getBusStopByBusStopCode(map.getBusStopCode());
            stop.setDistance(distance * 1000); // Distance in KM, set to M
            stop.setHasDistance(true);

            stops.add(stop);
        }
        Intent sendForMapParsingIntent = new Intent(BusStopNearbyFragment.RECEIVE_NEARBY_STOPS_EVENT);
        Type listType = new TypeToken<ArrayList<BusStopJSON>>() {}.getType();
        sendForMapParsingIntent.putExtra("data", gson.toJson(stops, listType));
        context.runOnUiThread(() -> {
            LocalBroadcastManager.getInstance(context).sendBroadcast(sendForMapParsingIntent);
            adapter.updateAdapter(stops);
            adapter.notifyDataSetChanged();
        });
        return 0;
    }

    public void onPostExecute(Integer errorCode) {
        Context context = contextRef.get();
        if (except != null && errorCode != 0) {
            LogHelper.e(TAG, "Exception occurred (" + except.getMessage() + ")");
            if (except instanceof SocketTimeoutException) {
                Toast.makeText(context, R.string.toast_message_timeout_distance_api, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, except.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}

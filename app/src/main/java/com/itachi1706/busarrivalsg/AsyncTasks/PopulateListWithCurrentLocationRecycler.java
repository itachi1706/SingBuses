package com.itachi1706.busarrivalsg.AsyncTasks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.itachi1706.appupdater.Util.ValidationHelper;
import com.itachi1706.busarrivalsg.Database.BusStopsDB;
import com.itachi1706.busarrivalsg.Fragments.BusStopNearbyFragment;
import com.itachi1706.busarrivalsg.GsonObjects.Distance;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusStopJSON;
import com.itachi1706.busarrivalsg.R;
import com.itachi1706.busarrivalsg.RecyclerViews.BusStopRecyclerAdapter;
import com.itachi1706.busarrivalsg.Util.StaticVariables;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Kenneth on 20/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.AsyncTasks
 */
public class PopulateListWithCurrentLocationRecycler extends AsyncTask<Location, Void, String> {

    private WeakReference<Context> contextRef;
    private BusStopsDB db;
    private BusStopRecyclerAdapter adapter;
    private Exception except;

    public PopulateListWithCurrentLocationRecycler(Context context, BusStopsDB db, BusStopRecyclerAdapter adapter) {
        this.contextRef = new WeakReference<>(context);
        this.db = db;
        this.adapter = adapter;
    }

    @Override
    protected String doInBackground(Location... locate) {
        Location location = locate[0];
        Context context = contextRef.get();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int limit = Integer.parseInt(sp.getString("nearbyStopsCount", "20"));
        // Get validation stuff
        String signature = ValidationHelper.getSignatureForValidation(context);
        String url = "http://api.itachi1706.com/api/mobile/nearestBusStop.php?location=" + location.getLatitude() + "," + location.getLongitude() + "&limit=" + limit;
        Log.d("CURRENT-LOCATION", url); // Don't print the signature out
        url += "&sig=" + signature + "&package=" + context.getPackageName();
        String tmp = "";
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
            except = e;
        }
        return tmp;
    }

    protected void onPostExecute(String json) {
        Context context = contextRef.get();
        if (except != null) {
            if (except instanceof SocketTimeoutException) {
                Toast.makeText(context, R.string.toast_message_timeout_distance_api, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, except.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            //Go parse it
            Gson gson = new Gson();
            Log.d("CURRENT-LOCATION", json);
            if (!StaticVariables.checkIfYouGotJsonString(json)) {
                //Invalid JSON string
                Toast.makeText(context, R.string.toast_message_invalid_json, Toast.LENGTH_SHORT).show();
                return;
            }
            Distance distArray = gson.fromJson(json, Distance.class);
            Distance.DistanceItem[] results = distArray.getResults();
            ArrayList<BusStopJSON> stops = new ArrayList<>();
            for (Distance.DistanceItem map : results) {
                float distance = map.getDist();
                BusStopJSON stop = db.getBusStopByBusStopCode(map.getBusStopCode());
                stop.setDistance(distance);

                stops.add(stop);
                Intent sendForMapParsingIntent = new Intent(BusStopNearbyFragment.RECEIVE_NEARBY_STOPS_EVENT);
                Type listType = new TypeToken<ArrayList<BusStopJSON>>() {}.getType();
                sendForMapParsingIntent.putExtra("data", gson.toJson(stops, listType));
                LocalBroadcastManager.getInstance(context).sendBroadcast(sendForMapParsingIntent);
                adapter.updateAdapter(stops);
                adapter.notifyDataSetChanged();
            }
        }
    }
}

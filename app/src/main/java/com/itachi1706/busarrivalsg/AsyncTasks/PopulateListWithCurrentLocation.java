package com.itachi1706.busarrivalsg.AsyncTasks;

import android.app.Activity;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.itachi1706.busarrivalsg.Database.BusStopsDB;
import com.itachi1706.busarrivalsg.GsonObjects.GooglePlaces.OnlineGMapsAPIArray;
import com.itachi1706.busarrivalsg.GsonObjects.GooglePlaces.OnlineGMapsJsonObject;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusStopJSON;
import com.itachi1706.busarrivalsg.ListViews.BusStopListView;
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
public class PopulateListWithCurrentLocation extends AsyncTask<Location, Void, String> {

    Activity activity;
    BusStopsDB db;
    BusStopListView adapter;
    Exception except;

    Location location;

    public PopulateListWithCurrentLocation(Activity activity, BusStopsDB db, BusStopListView adapter){
        this.activity = activity;
        this.db = db;
        this.adapter = adapter;
    }

    @Override
    protected String doInBackground(Location... locate) {
        location = locate[0];
        String url = "http://api.itachi1706.com/api/nearbyPlaces.php?location=" + location.getLatitude() + "," + location.getLongitude();
        String tmp = "";
        Log.d("CURRENT-LOCATION", url);
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
        if (except != null) {
            if (except instanceof SocketTimeoutException) {
                Toast.makeText(activity, "Google did not respond after the period of time", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, except.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            //Go parse it
            Gson gson = new Gson();
            Log.d("CURRENT-LOCATION", json);
            if (!StaticVariables.checkIfYouGotJsonString(json)) {
                //Invalid JSON string
                Toast.makeText(activity, "Invalid JSON. Please try again", Toast.LENGTH_SHORT).show();
                return;
            }
            OnlineGMapsAPIArray array = gson.fromJson(json, OnlineGMapsAPIArray.class);
            if (!array.getStatus().equalsIgnoreCase("OK")) {
                Toast.makeText(activity, "Invalid request. Please try again", Toast.LENGTH_SHORT).show();
                return;
            }
            OnlineGMapsJsonObject[] maps = array.getResults();
            ArrayList<BusStopJSON> stops = new ArrayList<>();
            for (OnlineGMapsJsonObject map : maps) {
                String name = map.getName();
                ArrayList<BusStopJSON> stopsTmp = db.getBusStopsByStopName(name);
                BusStopJSON stop = null;
                if (stopsTmp.size() == 0) {
                    Log.e("LOCATE", "Bus Stop not found in database, ignoring D:");
                    continue;
                }
                else if (stopsTmp.size() > 1 && stops.size() >= 1){
                    //Do stuff validating
                    //TODO HACKY FIX, MAYBE GET SOMETHING MORE LEGIT AS THIS ISNT EXACTLY CORRECT
                    boolean checks = false;
                    for (BusStopJSON check : stopsTmp){
                        char a = stops.get(0).getCode().charAt(0);
                        char b = check.getCode().charAt(0);
                        Log.d("CHECK", check.getBusStopName() + " [" + a + "|" + b + "]");
                        char c = stops.get(0).getCode().charAt(1);
                        char d = check.getCode().charAt(1);
                        if (a == b && c == d){
                            Log.d("CHECK-FOUND", check.getBusStopName() + " [" + a + "|" + b + "]");
                            stop = check;
                            checks = true;
                            break;
                        }
                    }
                    if (!checks)
                        stop = stopsTmp.get(0);
                } else {
                    stop = stopsTmp.get(0);
                }

                if (!map.getVicinity().equalsIgnoreCase("Singapore")){
                    Log.e("LOCATE", "Bus Stop not in Singapore, ignoring...");
                    continue;
                }

                //Get Location of Bus Stop
                double lat = map.getGeometry().getLocation().getLat();
                double lng = map.getGeometry().getLocation().getLng();
                Location locate = new Location("");
                locate.setLongitude(lng);
                locate.setLatitude(lat);

                //Calculate distance
                float distance = location.distanceTo(locate);
                stop.setDistance(distance);
                stop.setHasDistance(true);

                stops.add(stop);
                adapter.updateAdapter(stops);
                adapter.notifyDataSetChanged();
            }
        }
    }
}

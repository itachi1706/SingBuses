package com.itachi1706.busarrivalsg.AsyncTasks;

import android.app.Activity;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.itachi1706.busarrivalsg.Database.BusStopsDB;
import com.itachi1706.busarrivalsg.Database.BusStopsGeoDB;
import com.itachi1706.busarrivalsg.GsonObjects.GooglePlaces.OnlineGMapsAPIArray;
import com.itachi1706.busarrivalsg.GsonObjects.GooglePlaces.OnlineGMapsJsonObject;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusStopJSON;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusStopsGeoObject;
import com.itachi1706.busarrivalsg.R;
import com.itachi1706.busarrivalsg.RecyclerViews.BusStopRecyclerAdapter;
import com.itachi1706.busarrivalsg.Util.StaticVariables;

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
public class PopulateListWithCurrentLocationRecycler extends AsyncTask<Location, Void, String> {

    Activity activity;
    BusStopsDB db;
    BusStopsGeoDB geoDB;
    BusStopRecyclerAdapter adapter;
    Exception except;

    Location location;

    public PopulateListWithCurrentLocationRecycler(Activity activity, BusStopsDB db, BusStopsGeoDB geoDB, BusStopRecyclerAdapter adapter){
        this.activity = activity;
        this.db = db;
        this.adapter = adapter;
        this.geoDB = geoDB;
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
                Toast.makeText(activity, R.string.toast_message_timeout_google_places_api, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, except.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            //Go parse it
            Gson gson = new Gson();
            Log.d("CURRENT-LOCATION", json);
            if (!StaticVariables.checkIfYouGotJsonString(json)) {
                //Invalid JSON string
                Toast.makeText(activity, R.string.toast_message_invalid_json, Toast.LENGTH_SHORT).show();
                return;
            }
            OnlineGMapsAPIArray array = gson.fromJson(json, OnlineGMapsAPIArray.class);
            if (!array.getStatus().equalsIgnoreCase("OK")) {
                Toast.makeText(activity, R.string.toast_message_invalid_request, Toast.LENGTH_SHORT).show();
                return;
            }
            OnlineGMapsJsonObject[] maps = array.getResults();
            ArrayList<BusStopJSON> stops = new ArrayList<>();
            for (OnlineGMapsJsonObject map : maps) {
                String name = map.getName();
                ArrayList<BusStopJSON> stopsTmp = db.getBusStopsByStopName(name);
                BusStopJSON stop;
                if (stopsTmp == null){
                    Log.e("LOCATE", "Something went wrong here");
                    Toast.makeText(activity, R.string.toast_message_invalid_data, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (stopsTmp.size() == 0) {
                    Log.e("LOCATE", activity.getString(R.string.toast_message_invalid_bus_stop));
                    continue;
                }
                else if (stopsTmp.size() > 1){
                    //Do stuff validating
                    stop = validate(map, stopsTmp);

                    if (stop == null) {
                        //Unable to verify bus stop with new method, fallbacking to legacy method
                        Log.e("VALIDATION", "Unable to validate location, Fallbacking to legacy method");
                        stop = legacyVerifyMethod(stops, stopsTmp);
                    }
                } else {
                    Log.i("VALIDATION", "Validation not needed for " + stopsTmp.get(0).getCode());
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

    /**
     * Do Validation to ensure that we select the right bus stop in the off chance that there is more than 1 bus stop
     * @param map Google Maps Data
     * @param stopsTmp List of bus stops with the road name specified
     * @return returns the bus stop object if it found and validated one, else it will return null
     */
    @Nullable
    private BusStopJSON validate(OnlineGMapsJsonObject map, ArrayList<BusStopJSON> stopsTmp){
        //Each Bus Stop found in database
        Log.d("VALIDATION", "Validation Size: " + stopsTmp.size());
        for (BusStopJSON validation : stopsTmp){
            BusStopsGeoObject geo = geoDB.getBusStopByBusStopCode(validation.getCode());
            //Only 1 object
            if (geo == null){
                continue;
            }

            Log.d("VALIDATE-LAT", "DB Lat: " + geo.getLat() + " | GMaps Lat: " + map.getGeometry().getLocation().getLat());
            Log.d("VALIDATE-LNG", "DB Lng: " + geo.getLng() + " | GMaps Lng: " + map.getGeometry().getLocation().getLng());

            /**
             * According to http://gis.stackexchange.com/questions/8650/how-to-measure-the-accuracy-of-latitude-and-longitude
             * 4 decimal place has an accuracy level of up to 11m which hopefully is sufficient enough for us
             * (Don't think there's any bus stops thats less than 11m apart (besides opposite stops)
             */
            String lat = String.format("%.4f", Double.parseDouble(geo.getLat()));
            String lng = String.format("%.4f", Double.parseDouble(geo.getLng()));
            String mapLat = String.format("%.4f", map.getGeometry().getLocation().getLat());
            String mapLng = String.format("%.4f", map.getGeometry().getLocation().getLng());
            Log.d("VALIDATION", "Validating with " + geo.getNo());
            Log.d("VALIDATE-LAT", "After Formatting DB Lat: " + lat + " | GMaps Lat: " + mapLat);
            Log.d("VALIDATE-LNG", "After Formatting DB Lng: " + lng + " | GMaps Lng: " + mapLng);
            if (lat.equalsIgnoreCase(mapLat) && lng.equalsIgnoreCase(mapLng)){
                Log.i("VALIDATION", "Found Location at code " + validation.getCode());
                return validation;
            }
        }

        return null;
    }

    /**
     * If new Verification Method fails, falls back to this EXTREMELY hacky method :(
     * Hopefully the new verification method never fails alright :P
     * @param stops Current Data Saved
     * @param stopsTmp The list of bus stops with the road name specified
     * @return Hacky Bus Stop data
     */
    private BusStopJSON legacyVerifyMethod(ArrayList<BusStopJSON> stops, ArrayList<BusStopJSON> stopsTmp){
        if (stops.size() == 0){
            return stopsTmp.get(0);
        }
        BusStopJSON stop = new BusStopJSON();
        boolean checks = false;
        for (BusStopJSON check : stopsTmp){
            char a = stops.get(0).getCode().charAt(0);
            char b = check.getCode().charAt(0);
            Log.d("LEGACY-CHECK", check.getBusStopName() + " [" + a + "|" + b + "]");
            char c = stops.get(0).getCode().charAt(1);
            char d = check.getCode().charAt(1);
            if (a == b && c == d){
                Log.d("LEGACY-CHECK-FOUND", check.getBusStopName() + " [" + a + "|" + b + "]");
                stop = check;
                checks = true;
                break;
            }
        }
        if (!checks)
            stop = stopsTmp.get(0);

        return stop;
    }
}

package com.itachi1706.busarrivalsg.Services;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.itachi1706.busarrivalsg.gsonObjects.offline.BusArrayJSON;
import com.itachi1706.busarrivalsg.gsonObjects.offline.BusJSON;
import com.itachi1706.busarrivalsg.objects.BusServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by Kenneth on 17/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg
 */
public class BusStorage {

    private static final String TAG = "BusStorage";

    public static void addNewBus(BusServices bus, SharedPreferences prefs){
        String serviceNo = bus.getServiceNo();
        String operator = bus.getOperator();
        String stopID = bus.getStopID();
        String stopName = bus.getStopName();

        JSONArray arr = getExistingJSONString(prefs);
        JSONObject obj = new JSONObject();
        JSONObject main = new JSONObject();
        try {
            obj.put("service", serviceNo);
            obj.put("operator", operator);
            obj.put("stop", stopID);
            obj.put("stopName", stopName);
            arr.put(obj);
            main.put("storage",arr);
            prefs.edit().putString("stored", main.toString()).apply();
        } catch (JSONException e) {
            Log.e(TAG, "Error adding new bus to storage");
            Log.e(TAG, "Error message: " + e.getLocalizedMessage());
        }
    }

    private static JSONArray getExistingJSONString(SharedPreferences pref) {
        String json = pref.getString("stored", null);
        if (json == null) {
            return new JSONArray();
        }
        try {
            JSONObject obj = new JSONObject(json);
            return obj.getJSONArray("storage");
        } catch (JSONException e) {
            Log.e(TAG, "Error getting existing JSON String");
            Log.e(TAG, "Error message: " + e.getLocalizedMessage());
        }
        return new JSONArray();
    }

    public static ArrayList<BusServices> getStoredBuses(SharedPreferences pref){
        String json = pref.getString("stored", "-");
        if (json.equals("-")){
            return null;
        }

        ArrayList<BusServices> services = new ArrayList<>();

        Gson gson = new Gson();
        BusArrayJSON busArray = gson.fromJson(json, BusArrayJSON.class);
        for (BusJSON b : Objects.requireNonNull(busArray.getStorage())){
            BusServices bs = new BusServices();
            bs.setOperator(b.getOperator());
            bs.setServiceNo(b.getService());
            bs.setStopID(b.getStop());
            if (b.getStopName() != null)
                bs.setStopName(b.getStopName());
            bs.setObtainedNextData(false);
            services.add(bs);
        }

        return services;
    }

    public static void updateBusJSON(SharedPreferences pref, ArrayList<BusServices> newServices){
        pref.edit().remove("stored").apply();
        if (newServices.size() != 0) {
            for (BusServices s : newServices) {
                addNewBus(s, pref);
            }
        }
    }

    public static boolean hasFavourites(SharedPreferences pref){
        String check = pref.getString("stored", "wot");
        return !check.equals("wot");
    }
}

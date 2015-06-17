package com.itachi1706.busarrivalsg;

import android.content.SharedPreferences;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Kenneth on 17/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg
 */
public class BusStorage {

    public static void addNewBus(BusServices bus, SharedPreferences prefs){
        String serviceNo = bus.getServiceNo();
        String operator = bus.getOperator();
        String stopID = bus.getStopID();

        JSONArray arr = getExistingJSONString(prefs);
        JSONObject obj = new JSONObject();
        JSONObject main = new JSONObject();
        try {
            obj.put("service", serviceNo);
            obj.put("operator", operator);
            obj.put("stop", stopID);
            arr.put(obj);
            main.put("storage",arr);
            prefs.edit().putString("stored", main.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
        for (BusJSON b : busArray.getStorage()){
            BusServices bs = new BusServices();
            bs.setOperator(b.getOperator().toString());
            bs.setServiceNo(b.getService().toString());
            bs.setStopID(b.getStop().toString());
            services.add(bs);
        }

        return services;
    }
}

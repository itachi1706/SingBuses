package com.itachi1706.busarrivalsg;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Kenneth on 17/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg
 */
public class BusArrayJSON {

    private BusJSON[] storage;

    public BusJSON[] getStorage() {
        return storage;
    }

    @Override
    public String toString() {
        return "BusArrayObject{" +
                "storage=" + storage + "}";
    }
}

class BusJSON {
    private JsonObject service, operator, stop;

    JsonObject getService() {
        return service;
    }

    JsonObject getOperator() {
        return operator;
    }

    JsonObject getStop() {
        return stop;
    }

    @Override
    public String toString() {
        return "BusObject{" +
                "service=" + service + ", operator=" + operator + "stop=" + stop + "}";
    }
}

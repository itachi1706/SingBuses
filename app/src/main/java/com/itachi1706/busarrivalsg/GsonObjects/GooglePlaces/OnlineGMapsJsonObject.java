package com.itachi1706.busarrivalsg.GsonObjects.GooglePlaces;

import com.google.gson.JsonArray;

/**
 * Created by Kenneth on 18/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.GsonObjects
 */
public class OnlineGMapsJsonObject {
    private String id, name, place_id, reference, scope, vicinity;
    private JsonArray types;

    public String getId() {
        return id;
    }

    /**
     * Returns the current road name of the bus stop
     * @return Bus Stop's Road Name
     */
    public String getName() {
        return name;
    }

    public String getPlace_id() {
        return place_id;
    }

    public String getReference() {
        return reference;
    }

    public String getScope() {
        return scope;
    }

    public String getVicinity() {
        return vicinity;
    }

    public JsonArray getTypes() {
        return types;
    }
}

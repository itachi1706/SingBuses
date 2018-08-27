package com.itachi1706.busarrivalsg.GsonObjects.GooglePlaces;

/**
 * Created by Kenneth on 18/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.GsonObjects
 */
@Deprecated
public class OnlineGMapsAPIArray {

    private String status;
    private OnlineGMapsJsonObject[] results;

    public OnlineGMapsJsonObject[] getResults() {
        return results;
    }

    public String getStatus() {
        return status;
    }
}

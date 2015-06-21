package com.itachi1706.busarrivalsg.GsonObjects.LTA;

/**
 * Created by Kenneth on 18/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.GsonObjects.LTA
 */
public class BusArrivalMain {

    private String BusStopID;
    private BusArrivalArrayObject[] Services;

    public BusArrivalArrayObject[] getServices() {
        return Services;
    }

    public String getBusStopID() {
        return BusStopID;
    }
}

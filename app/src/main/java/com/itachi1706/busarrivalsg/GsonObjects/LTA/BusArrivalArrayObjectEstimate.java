package com.itachi1706.busarrivalsg.GsonObjects.LTA;

/**
 * Created by Kenneth on 18/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.GsonObjects.LTA
 */
public class BusArrivalArrayObjectEstimate {
    private String EstimatedArrival, Load, Feature;

    //Going to be implemented from 12 November
    private float Latitude, Longitude;
    private int VisitNumber;
    private String Monitored;

    public String getFeature() {
        return Feature;
    }

    public String getLoad() {
        return Load;
    }

    public String getEstimatedArrival() {
        return EstimatedArrival;
    }

    public float getLatitude() {
        return Latitude;
    }

    public boolean hasLatitude(){
        return Latitude != 0;
    }

    public float getLongitude() {
        return Longitude;
    }

    public boolean hasLongitude(){
        return Longitude != 0;
    }

    public int getVisitNumber() {
        return VisitNumber;
    }

    public String getMonitored() {
        return Monitored;
    }

    public boolean getMonitoredStatus() {
        return Monitored == null || Monitored.equalsIgnoreCase("true");
    }
}

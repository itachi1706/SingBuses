package com.itachi1706.busarrivalsg.gsonObjects.sgLTA;

/**
 * Created by Kenneth on 18/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.GsonObjects.LTA
 */
public class BusStopJSON {

    private String BusStopCode, RoadName, Description, Services;
    private int timestamp;
    private double Latitude, Longitude;

    private boolean hasDistance = false;
    private float distance;



    public String getBusStopCode() {
        return BusStopCode;
    }

    public String getRoadName() {
        return RoadName;
    }

    public String getDescription() {
        return Description;
    }

    public void setBusStopCode(String code) {
        BusStopCode = code;
    }

    public void setRoadName(String road) {
        RoadName = road;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public double getLatitude() {
        return Latitude;
    }

    public void setLatitude(double latitude) {
        Latitude = latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double longitude) {
        Longitude = longitude;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isHasDistance() {
        return hasDistance;
    }

    public void setHasDistance(boolean hasDistance) {
        this.hasDistance = hasDistance;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public String getServices() {
        return (Services == null) ? "" : Services;
    }

    public void setServices(String services) {
        this.Services = services;
    }
}

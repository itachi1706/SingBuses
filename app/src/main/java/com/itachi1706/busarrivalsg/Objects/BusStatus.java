package com.itachi1706.busarrivalsg.Objects;

/**
 * Created by Kenneth on 17/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg
 */
public class BusStatus {

    private String estimatedArrival;
    private int load;   // 0-NULL,1-Seats Available,2-Limited Seating,3-No Seating
    private boolean isWheelChairAccessible;

    //Going to be implemented from 12 November
    private double latitude = -11, longitude = -11;
    private int visitNumber;
    private boolean isMonitored = true;

    public boolean isWheelChairAccessible() {
        return isWheelChairAccessible;
    }

    public void setIsWheelChairAccessible(boolean isWheelChairAccessible) {
        this.isWheelChairAccessible = isWheelChairAccessible;
    }

    public void setIsWheelChairAccessible(String isWheelCharAccessible){
        switch (isWheelCharAccessible){
            case "WAB": this.isWheelChairAccessible = true; break;
            default: this.isWheelChairAccessible = false; break;
        }
    }

    public String getEstimatedArrival() {
        return estimatedArrival;
    }

    public void setEstimatedArrival(String estimatedArrival) {
        this.estimatedArrival = estimatedArrival;
    }

    public int getLoad() {
        return load;
    }

    public void setLoad(int load) {
        this.load = load;
    }

    public void setLoad(String load){
        switch (load){
            case "Seats Available": this.load = 1; break;
            case "Standing Available": this.load = 2; break;
            case "Limited Standing": this.load = 3; break;
            default: this.load = 0; break;
        }
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getVisitNumber() {
        return visitNumber;
    }

    public void setVisitNumber(int visitNumber) {
        this.visitNumber = visitNumber;
    }

    public boolean isMonitored() {
        return isMonitored;
    }

    public void setIsMonitored(boolean isMonitored) {
        this.isMonitored = isMonitored;
    }
}

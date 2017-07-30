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

    // Implemented as of 30 July 2017
    private String originatingID, terminatingID;
    private int busType;

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

    public void setLoad(String load){
        switch (load){
            case "SEA": this.load = CommonEnums.BUS_SEATS_AVAIL; break;
            case "SDA": this.load = CommonEnums.BUS_STANDING_AVAIL; break;
            case "LSD": this.load = CommonEnums.BUS_LIMITED_SEATS; break;
            default: this.load = CommonEnums.UNKNOWN; break;
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

    public String getOriginatingID() {
        return originatingID;
    }

    public void setOriginatingID(String originatingID) {
        this.originatingID = originatingID;
    }

    public String getTerminatingID() {
        return terminatingID;
    }

    public void setTerminatingID(String terminatingID) {
        this.terminatingID = terminatingID;
    }

    public int getBusType() {
        return busType;
    }

    public void setBusType(String busType) {
        switch (busType) {
            case "SD": this.busType = CommonEnums.BUS_SINGLE_DECK; break;
            case "DD": this.busType = CommonEnums.BUS_DOUBLE_DECK; break;
            case "BD": this.busType = CommonEnums.BUS_BENDY; break;
            default: this.busType = CommonEnums.UNKNOWN; break;
        }
    }
}

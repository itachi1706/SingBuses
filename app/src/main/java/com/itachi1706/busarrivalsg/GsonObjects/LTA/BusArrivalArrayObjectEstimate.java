package com.itachi1706.busarrivalsg.GsonObjects.LTA;

/**
 * Created by Kenneth on 18/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.GsonObjects.LTA
 */
public class BusArrivalArrayObjectEstimate {
    private String EstimatedArrival, Load, Feature;

    //Going to be implemented from 12 November
    private String Latitude = "-11", Longitude = "-11"; //return double option
    private String VisitNumber; //return int option

    public String getFeature() {
        return Feature;
    }

    public String getLoad() {
        return (Load == null) ? "" : Load;
    }

    public String getEstimatedArrival() {
        return EstimatedArrival;
    }

    public double getLatitude() {
        try {
            return Double.parseDouble(Latitude);
        } catch (NumberFormatException e){
            return -11;
        }
    }

    public double getLongitude() {
        try {
            return Double.parseDouble(Longitude);
        } catch (NumberFormatException e){
            return -11;
        }
    }

    public int getVisitNumber() {
        try {
            return Integer.parseInt(VisitNumber);
        } catch (NumberFormatException e){
            return 0;
        }
    }

    public String getLatitudeString() {
        return Latitude;
    }

    public boolean hasLatitude(){ return Latitude.length() != 0; }

    public String getLongitudeString() {
        return Longitude;
    }

    public boolean hasLongitude(){
        return Longitude.length() != 0;
    }

    public String getVisitNumberString() {
        return VisitNumber;
    }

    public boolean isWheelchairAccessible() {
        return Feature != null && Feature.contains("WAB");
    }
}

package com.itachi1706.busarrivalsg.GsonObjects.LTA;

import com.itachi1706.busarrivalsg.Objects.CommonEnums;

/**
 * Created by Kenneth on 18/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.GsonObjects.LTA
 */
public class BusArrivalArrayObjectEstimate {
    private String EstimatedArrival, Load, Feature;

    // Implemented from 12 November 2016
    private String Latitude = "-11", Longitude = "-11"; //return double option
    private String VisitNumber; //return int option

    // Implemented from 30 July 2017
    private String OriginCode, DestinationCode, Type;

    public String getFeature() {
        return Feature;
    }

    public String getLoad() {
        return (Load == null) ? "" : Load;
    }

    public int getLoadInt() {
        if (Load == null) return CommonEnums.UNKNOWN;
        switch (Load){
            case "SEA": return CommonEnums.BUS_SEATS_AVAIL;
            case "SDA": return CommonEnums.BUS_STANDING_AVAIL;
            case "LSD": return CommonEnums.BUS_LIMITED_SEATS;
            default: return CommonEnums.UNKNOWN;
        }
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

    public String getOriginCode() {
        return OriginCode;
    }

    public String getDestinationCode() {
        return DestinationCode;
    }

    public String getType() {
        return Type;
    }

    public int getTypeInt() {
        switch (Type) {
            case "SD": return CommonEnums.BUS_SINGLE_DECK;
            case "DD": return CommonEnums.BUS_DOUBLE_DECK;
            case "BD": return CommonEnums.BUS_BENDY;
            default: return CommonEnums.UNKNOWN;
        }
    }
}

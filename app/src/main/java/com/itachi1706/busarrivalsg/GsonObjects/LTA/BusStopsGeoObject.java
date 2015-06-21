package com.itachi1706.busarrivalsg.GsonObjects.LTA;

/**
 * Created by Kenneth on 21/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.GsonObjects.LTA
 */
public class BusStopsGeoObject {
    private String no, lat, lng, name;

    public String getNo() {
        return no;
    }

    public String getLat() {
        return lat;
    }

    public String getLng() {
        return lng;
    }

    public String getName() {
        return name;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public void setName(String name) {
        this.name = name;
    }
}

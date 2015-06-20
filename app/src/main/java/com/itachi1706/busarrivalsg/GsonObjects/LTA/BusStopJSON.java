package com.itachi1706.busarrivalsg.GsonObjects.LTA;

/**
 * Created by Kenneth on 18/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.GsonObjects.LTA
 */
public class BusStopJSON {

    private String Code, Road, Description, Summary, CreateDate;
    private int BusStopCodeID;

    public int getBusStopCodeID() {
        return BusStopCodeID;
    }

    public String getCode() {
        return Code;
    }

    public String getRoad() {
        return Road;
    }

    public String getBusStopName() {
        return Description;
    }

    public String getSummary() {
        return Summary;
    }

    public String getCreateDate() {
        return CreateDate;
    }

    public void setCode(String code) {
        Code = code;
    }

    public void setRoad(String road) {
        Road = road;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public void setSummary(String summary) {
        Summary = summary;
    }

    public void setCreateDate(String createDate) {
        CreateDate = createDate;
    }

    public void setBusStopCodeID(int busStopCodeID) {
        BusStopCodeID = busStopCodeID;
    }
}

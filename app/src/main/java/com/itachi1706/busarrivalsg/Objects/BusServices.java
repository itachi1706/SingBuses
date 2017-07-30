package com.itachi1706.busarrivalsg.Objects;

/**
 * Created by Kenneth on 17/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg
 */
public class BusServices {

    private String serviceNo,operator,stopID;
    private BusStatus currentBus,nextBus;
    private boolean obtainedNextData;
    private long time;
    private String stopName;

    @Deprecated private String operatingStatus;

    //Going to be implemented from 12 November
    private BusStatus subsequentBus;

    public BusStatus getNextBus() {
        return nextBus;
    }

    public void setNextBus(BusStatus nextBus) {
        this.nextBus = nextBus;
    }

    public BusStatus getCurrentBus() {
        return currentBus;
    }

    public void setCurrentBus(BusStatus currentBus) {
        this.currentBus = currentBus;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getServiceNo() {
        return serviceNo;
    }

    public void setServiceNo(String serviceNo) {
        this.serviceNo = serviceNo;
    }

    public String getStopID() {
        return stopID;
    }

    public void setStopID(String stopID) {
        this.stopID = stopID;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isObtainedNextData() {
        return obtainedNextData;
    }

    public void setObtainedNextData(boolean obtainedNextData) {
        this.obtainedNextData = obtainedNextData;
    }

    @Deprecated
    public String getOperatingStatus() {
        return operatingStatus;
    }

    @Deprecated
    public void setOperatingStatus(String operatingStatus) {
        this.operatingStatus = operatingStatus;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public BusStatus getSubsequentBus() {
        return subsequentBus;
    }

    public void setSubsequentBus(BusStatus subsequentBus) {
        this.subsequentBus = subsequentBus;
    }
}

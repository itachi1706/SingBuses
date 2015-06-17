package com.itachi1706.busarrivalsg;

/**
 * Created by Kenneth on 17/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg
 */
public class BusServices {

    private String serviceNo,operator,stopID;
    private BusStatus currentBus,nextBus;

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
}

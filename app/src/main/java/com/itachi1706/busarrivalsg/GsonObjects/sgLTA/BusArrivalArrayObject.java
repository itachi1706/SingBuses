package com.itachi1706.busarrivalsg.gsonObjects.sgLTA;

/**
 * Created by Kenneth on 18/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.GsonObjects.LTA
 */
public class BusArrivalArrayObject {
    private String ServiceNo, Operator;
    private BusArrivalArrayObjectEstimate NextBus;

    // Implemented as of 30 July 2017
    private BusArrivalArrayObjectEstimate NextBus2, NextBus3;

    private String stopCode;
    private boolean svcStatus;

    public BusArrivalArrayObjectEstimate getNextBus2() {
        return NextBus2;
    }

    public BusArrivalArrayObjectEstimate getNextBus() {
        return NextBus;
    }

    public String getOperator() {
        return Operator;
    }

    public String getServiceNo() {
        return ServiceNo;
    }

    public String getStopCode() {
        return stopCode;
    }

    public void setStopCode(String stopCode) {
        this.stopCode = stopCode;
    }

    public BusArrivalArrayObjectEstimate getNextBus3() {
        return NextBus3;
    }

    public boolean isSvcStatus() {
        return svcStatus;
    }

    public void setSvcStatus(boolean svcStatus) {
        this.svcStatus = svcStatus;
    }
}

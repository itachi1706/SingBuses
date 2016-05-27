package com.itachi1706.busarrivalsg.GsonObjects.Offline;

/**
 * Created by Kenneth on 18/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.GsonObjects
 */
public class BusJSON {
    private String service, operator, stop, stopName;

    @Override
    public String toString() {
        return "BusObject{" +
                "service=" + service + ", operator=" + operator + ",stop=" + stop + ",stopName=" + stopName + "}";
    }

    public String getService(){ return this.service; }

    public String getOperator(){ return this.operator; }

    public String getStop(){ return this.stop; }

    public String getStopName(){ return this.stopName; }
}

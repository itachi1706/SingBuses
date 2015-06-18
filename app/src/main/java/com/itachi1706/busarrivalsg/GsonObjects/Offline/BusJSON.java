package com.itachi1706.busarrivalsg.GsonObjects.Offline;

import com.google.gson.JsonObject;

/**
 * Created by Kenneth on 18/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.GsonObjects
 */
public class BusJSON {
    private String service, operator, stop;

    @Override
    public String toString() {
        return "BusObject{" +
                "service=" + service + ", operator=" + operator + "stop=" + stop + "}";
    }

    public String getService(){ return this.service; }

    public String getOperator(){ return this.operator; }

    public String getStop(){ return this.stop; }
}

package com.itachi1706.busarrivalsg;

/**
 * Created by Kenneth on 17/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg
 */
public class BusStatus {

    private String estimatedArrival;
    private int load;   // 0-NULL,1-Seats Available,2-Limited Seating,3-No Seating
    private boolean isWheelChairAccessible;

    public boolean isWheelChairAccessible() {
        return isWheelChairAccessible;
    }

    public void setIsWheelChairAccessible(boolean isWheelChairAccessible) {
        this.isWheelChairAccessible = isWheelChairAccessible;
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

    public void setLoad(int load) {
        this.load = load;
    }
}

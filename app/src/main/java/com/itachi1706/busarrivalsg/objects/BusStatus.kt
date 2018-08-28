package com.itachi1706.busarrivalsg.objects

import com.itachi1706.busarrivalsg.Util.BusesUtil

/**
 * Created by Kenneth on 17/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg
 */
class BusStatus {

    var estimatedArrival: String? = null
    var load: Int = 0
        private set   // 0-NULL,1-Seats Available,2-Limited Seating,3-No Seating
    var isWheelChairAccessible: Boolean = false

    //Going to be implemented from 12 November
    var latitude = -11.0
    var longitude = -11.0
    var visitNumber: Int = 0

    // Implemented as of 30 July 2017
    var originatingID: String? = null
    var terminatingID: String? = null
    var busType: Int = 0
        private set

    fun setIsWheelChairAccessible(isWheelCharAccessible: String) {
        when (isWheelCharAccessible) {
            "WAB" -> this.isWheelChairAccessible = true
            else -> this.isWheelChairAccessible = false
        }
    }

    fun setLoad(load: String) {
        this.load = BusesUtil.getLoad(load)
    }

    fun setBusType(busType: String) {
        this.busType = BusesUtil.getType(busType)
    }
}

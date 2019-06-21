package com.itachi1706.busarrivalsg.objects

import com.itachi1706.busarrivalsg.util.BusesUtil

/**
 * Created by Kenneth on 17/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg
 */
class BusStatus (var estimatedArrival: String? = null, var load: Int = 0, var isWheelChairAccessible: Boolean = false, var latitude: Double = -11.0, var longitude: Double = -11.0,
                 var visitNumber: Int = 0, var originatingID: String? = null, var terminatingID: String? = null, var busType: Int = 0) {
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

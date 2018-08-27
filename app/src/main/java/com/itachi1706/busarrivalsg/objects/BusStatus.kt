package com.itachi1706.busarrivalsg.objects

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
        when (load) {
            "SEA" -> this.load = CommonEnums.BUS_SEATS_AVAIL
            "SDA" -> this.load = CommonEnums.BUS_STANDING_AVAIL
            "LSD" -> this.load = CommonEnums.BUS_LIMITED_SEATS
            else -> this.load = CommonEnums.UNKNOWN
        }
    }

    fun setBusType(busType: String) {
        when (busType) {
            "SD" -> this.busType = CommonEnums.BUS_SINGLE_DECK
            "DD" -> this.busType = CommonEnums.BUS_DOUBLE_DECK
            "BD" -> this.busType = CommonEnums.BUS_BENDY
            else -> this.busType = CommonEnums.UNKNOWN
        }
    }
}

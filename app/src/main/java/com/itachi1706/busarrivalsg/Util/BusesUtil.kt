package com.itachi1706.busarrivalsg.Util

import com.itachi1706.busarrivalsg.objects.CommonEnums

/**
 * Created by Kenneth on 28/8/2018.
 * for com.itachi1706.busarrivalsg.Util in SingBuses
 */
object BusesUtil {

    fun getLoad(load: String?): Int {
        if (load == null) return CommonEnums.UNKNOWN
        return when (load) {
            "SEA" -> CommonEnums.BUS_SEATS_AVAIL
            "SDA" -> CommonEnums.BUS_STANDING_AVAIL
            "LSD" -> CommonEnums.BUS_LIMITED_SEATS
            else -> CommonEnums.UNKNOWN
        }
    }

    fun getType(type: String): Int {
        return when (type) {
            "SD" -> CommonEnums.BUS_SINGLE_DECK
            "DD" -> CommonEnums.BUS_DOUBLE_DECK
            "BD" -> CommonEnums.BUS_BENDY
            else -> CommonEnums.UNKNOWN
        }
    }
}

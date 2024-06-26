@file:Suppress("PropertyName")

package com.itachi1706.busarrivalsg.gsonObjects.sgLTA

/**
 * Created by Kenneth on 18/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.gsonObjects.sgLTA
 */
data class BusArrivalMain(val BusStopCode: String? = null, val Services: Array<BusArrivalArrayObject>? = null, val CurrentTime: String? = null) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BusArrivalMain

        if (BusStopCode != other.BusStopCode) return false
        if (Services != null) {
            if (other.Services == null) return false
            if (!Services.contentEquals(other.Services)) return false
        } else if (other.Services != null) return false
        if (CurrentTime != other.CurrentTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = BusStopCode?.hashCode() ?: 0
        result = 31 * result + (Services?.contentHashCode() ?: 0)
        result = 31 * result + (CurrentTime?.hashCode() ?: 0)
        return result
    }
}

@file:Suppress("PropertyName")

package com.itachi1706.busarrivalsg.gsonObjects.sgLTA

import com.google.gson.annotations.SerializedName

/**
 * Created by Kenneth on 18/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.gsonObjects.sgLTA
 */
data class BusArrivalMain(
    @SerializedName("BusStopCode") val busStopCode: String? = null,
    @SerializedName("Services") val services: Array<BusArrivalArrayObject>? = null,
    @SerializedName("CurrentTime") val currentTime: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BusArrivalMain

        if (busStopCode != other.busStopCode) return false
        if (services != null) {
            if (other.services == null) return false
            if (!services.contentEquals(other.services)) return false
        } else if (other.services != null) return false
        if (currentTime != other.currentTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = busStopCode?.hashCode() ?: 0
        result = 31 * result + (services?.contentHashCode() ?: 0)
        result = 31 * result + (currentTime?.hashCode() ?: 0)
        return result
    }
}

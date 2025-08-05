package com.itachi1706.busarrivalsg.objects.gson

import com.google.gson.annotations.SerializedName

/**
 * Created by Kenneth on 27/8/2018.
 * for com.itachi1706.busarrivalsg.gsonObjects in SingBuses
 */
class Distance(var currentCoord: CurrentCoords? = null, var results: Array<DistanceItem>? = null) {
    data class CurrentCoords(var lat: String? = null, val lng: String? = null)
    data class DistanceItem(
        @SerializedName("BusStopCode") var busStopCode: String? = null,
        @SerializedName("Latitude") val latitude: Double = 0.toDouble(),
        @SerializedName("Longitude") val longitude: Double = 0.toDouble(),
        val dist: Float = 0.toFloat()
    )
}

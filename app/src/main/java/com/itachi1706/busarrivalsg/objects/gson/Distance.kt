package com.itachi1706.busarrivalsg.objects.gson

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

/**
 * Created by Kenneth on 27/8/2018.
 * for com.itachi1706.busarrivalsg.gsonObjects in SingBuses
 */
@Serializable
class Distance(var currentCoord: CurrentCoords? = null, var stops: Array<DistanceItem>? = null) {
    @Serializable data class CurrentCoords(var lat: String? = null, val lng: String? = null)
    @Serializable
    data class DistanceItem(
        @SerializedName("BusStopCode") var busStopCode: String? = null,
        @SerializedName("Latitude") val latitude: Double = 0.0,
        @SerializedName("Longitude") val longitude: Double = 0.0,
        val dist: Float = 0.0f
    )
}

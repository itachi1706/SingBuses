package com.itachi1706.busarrivalsg.objects.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Distance(var currentCoord: CurrentCoords? = null, var stops: Array<DistanceItem>? = null) {
    @Serializable data class CurrentCoords(val lat: Double = 0.0, val lng: Double = 0.0)
    @Serializable
    data class DistanceItem(
        @SerialName("BusStopCode") var busStopCode: String? = null,
        @SerialName("Latitude") val latitude: Double = 0.0,
        @SerialName("Longitude") val longitude: Double = 0.0,
        val dist: Float = 0.0f
    )
}

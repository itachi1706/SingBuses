package com.itachi1706.busarrivalsg.objects.json.ltasg

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class BusStopJSON(
    @SerialName("BusStopCode") var busStopCode: String = "",
    @SerialName("RoadName") var roadName: String = "",
    @SerialName("Description") var description: String = "",
    @SerialName("Services") var services: String = "",
    var timestamp: Int = 0,
    @SerialName("Latitude") var latitude: Double = 0.0,
    @SerialName("Longitude") var longitude: Double = 0.0,
    var isHasDistance: Boolean = false,
    var distance: Float = 0.0f
)

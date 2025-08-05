package com.itachi1706.busarrivalsg.objects.gson.ltasg

import com.google.gson.annotations.SerializedName

/**
 * Created by Kenneth on 18/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.gsonObjects.sgLTA
 */
class BusStopJSON(
    @SerializedName("BusStopCode") var busStopCode: String = "",
    @SerializedName("RoadName") var roadName: String = "",
    @SerializedName("Description") var description: String = "",
    @SerializedName("Services") var services: String = "",
    var timestamp: Int = 0,
    @SerializedName("Latitude") var latitude: Double = 0.0,
    @SerializedName("Longitude") var longitude: Double = 0.0,
    var isHasDistance: Boolean = false,
    var distance: Float = 0.0f
)

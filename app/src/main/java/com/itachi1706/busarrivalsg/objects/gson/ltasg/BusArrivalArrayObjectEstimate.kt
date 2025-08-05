package com.itachi1706.busarrivalsg.objects.gson.ltasg

import com.google.gson.annotations.SerializedName
import com.itachi1706.busarrivalsg.util.BusesUtil

/**
 * Created by Kenneth on 18/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.gsonObjects.sgLTA
 */
class BusArrivalArrayObjectEstimate(
    @SerializedName("EstimatedArrival") val estimatedArrival: String? = null,
    @SerializedName("Load") val load: String = "",
    @SerializedName("Feature") val feature: String = "",
    @SerializedName("Latitude") val latitude: String = "-11",
    @SerializedName("Longitude") val longitude: String = "-11",
    @SerializedName("VisitNumber") private val visitNumber: String = "0",
    @SerializedName("OriginCode") val originCode: String? = null,
    @SerializedName("DestinationCode") val destinationCode: String? = null,
    @SerializedName("Type") val type: String = ""
) {
    val loadInt: Int
        get() = BusesUtil.getLoad(load)

    val latitudeD: Double
        get() {
            return latitude.toDoubleOrNull() ?: -11.0
        }

    val longitudeD: Double
        get() {
            return longitude.toDoubleOrNull() ?: -11.0
        }

    val visitNumberD: Int
        get() {
            return visitNumber.toIntOrNull() ?: 0
        }

    val isWheelchairAccessible: Boolean
        get() = feature.contains("WAB")

    val typeInt: Int
        get() = BusesUtil.getType(type)
}

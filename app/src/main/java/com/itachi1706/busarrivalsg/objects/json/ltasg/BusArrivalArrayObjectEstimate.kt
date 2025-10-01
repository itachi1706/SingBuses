package com.itachi1706.busarrivalsg.objects.json.ltasg

import com.itachi1706.busarrivalsg.util.BusesUtil
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class BusArrivalArrayObjectEstimate(
    @SerialName("EstimatedArrival") val estimatedArrival: String? = null,
    @SerialName("Load") val load: String = "",
    @SerialName("Feature") val feature: String = "",
    @SerialName("Latitude") val latitude: String = "-11",
    @SerialName("Longitude") val longitude: String = "-11",
    @SerialName("VisitNumber") private val visitNumber: String = "0",
    @SerialName("OriginCode") val originCode: String? = null,
    @SerialName("DestinationCode") val destinationCode: String? = null,
    @SerialName("Type") val type: String = ""
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

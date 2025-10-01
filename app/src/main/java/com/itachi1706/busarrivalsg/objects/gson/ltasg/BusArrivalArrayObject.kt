package com.itachi1706.busarrivalsg.objects.gson.ltasg

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by Kenneth on 18/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.gsonObjects.sgLTA
 */
@Serializable
data class BusArrivalArrayObject(
    @SerialName("ServiceNo") val serviceNo: String = "",
    @SerialName("Operator") val operator: String = "",
    @SerialName("NextBus") val nextBus: BusArrivalArrayObjectEstimate? = null,
    @SerialName("NextBus2") val nextBus2: BusArrivalArrayObjectEstimate? = null,
    @SerialName("NextBus3") val nextBus3: BusArrivalArrayObjectEstimate? = null,
    var stopCode: String = "",
    var isSvcStatus: Boolean = false
)

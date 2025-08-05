package com.itachi1706.busarrivalsg.objects.gson.ltasg

import com.google.gson.annotations.SerializedName

/**
 * Created by Kenneth on 18/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.gsonObjects.sgLTA
 */
data class BusArrivalArrayObject(
    @SerializedName("ServiceNo") val serviceNo: String = "",
    @SerializedName("Operator") val operator: String = "",
    @SerializedName("NextBus") val nextBus: BusArrivalArrayObjectEstimate? = null,
    @SerializedName("NextBus2") val nextBus2: BusArrivalArrayObjectEstimate? = null,
    @SerializedName("NextBus3") val nextBus3: BusArrivalArrayObjectEstimate? = null,
    var stopCode: String = "",
    var isSvcStatus: Boolean = false
)

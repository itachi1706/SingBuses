@file:Suppress("PropertyName")

package com.itachi1706.busarrivalsg.gsonObjects.sgLTA

/**
 * Created by Kenneth on 18/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.gsonObjects.sgLTA
 */
data class BusArrivalArrayObject (val ServiceNo: String = "", val Operator: String = "", val NextBus: BusArrivalArrayObjectEstimate? = null, val NextBus2: BusArrivalArrayObjectEstimate? = null,
    val NextBus3: BusArrivalArrayObjectEstimate? = null, var stopCode: String = "", var isSvcStatus: Boolean = false)

@file:Suppress("PropertyName")

package com.itachi1706.busarrivalsg.gsonObjects.sgLTA

/**
 * Created by Kenneth on 18/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.GsonObjects.LTA
 */
class BusArrivalArrayObject {
    val ServiceNo: String? = null
    val Operator: String? = null
    val NextBus: BusArrivalArrayObjectEstimate? = null

    // Implemented as of 30 July 2017
    val NextBus2: BusArrivalArrayObjectEstimate? = null
    val NextBus3: BusArrivalArrayObjectEstimate? = null

    var stopCode: String? = null
    var isSvcStatus: Boolean = false
}

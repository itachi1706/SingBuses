package com.itachi1706.busarrivalsg.GsonObjects.LTA

/**
 * Created by Kenneth on 18/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.GsonObjects.LTA
 */
class BusArrivalArrayObject {
    val serviceNo: String? = null
    val operator: String? = null
    val nextBus: BusArrivalArrayObjectEstimate? = null

    // Implemented as of 30 July 2017
    val nextBus2: BusArrivalArrayObjectEstimate? = null
    val nextBus3: BusArrivalArrayObjectEstimate? = null

    var stopCode: String? = null
    var isSvcStatus: Boolean = false
}

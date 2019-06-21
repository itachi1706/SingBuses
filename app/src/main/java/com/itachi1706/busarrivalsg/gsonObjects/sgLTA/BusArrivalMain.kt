@file:Suppress("PropertyName")

package com.itachi1706.busarrivalsg.gsonObjects.sgLTA

/**
 * Created by Kenneth on 18/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.gsonObjects.sgLTA
 */
@Suppress("ArrayInDataClass")
data class BusArrivalMain(val BusStopCode: String? = null, val Services: Array<BusArrivalArrayObject>? = null, val CurrentTime: String? = null)

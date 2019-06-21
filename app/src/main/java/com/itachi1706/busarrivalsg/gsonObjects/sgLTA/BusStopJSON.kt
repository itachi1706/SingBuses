@file:Suppress("PropertyName")

package com.itachi1706.busarrivalsg.gsonObjects.sgLTA

/**
 * Created by Kenneth on 18/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.gsonObjects.sgLTA
 */
class BusStopJSON (var BusStopCode: String? = null, var RoadName: String? = null, var Description: String? = null, var Services: String? = "", var timestamp: Int = 0,
    var Latitude: Double = 0.toDouble(), var Longitude: Double = 0.toDouble(), var isHasDistance: Boolean = false, var distance: Float = 0.toFloat())

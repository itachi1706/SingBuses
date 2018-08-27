package com.itachi1706.busarrivalsg.GsonObjects.LTA

/**
 * Created by Kenneth on 18/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.GsonObjects.LTA
 */
class BusStopJSON {

    var code: String? = null
    var road: String? = null
    var busStopName: String? = null
        private set
    var services: String? = null
        get() = if (field == null) "" else field
    var timestamp: Int = 0
    var latitude: Double = 0.toDouble()
    var longitude: Double = 0.toDouble()

    var isHasDistance = false
    var distance: Float = 0.toFloat()

    fun setDescription(description: String) {
        busStopName = description
    }
}

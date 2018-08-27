package com.itachi1706.busarrivalsg.GsonObjects

/**
 * Created by Kenneth on 27/8/2018.
 * for com.itachi1706.busarrivalsg.GsonObjects in SingBuses
 */
class Distance {

    var currentCoord: CurrentCoords? = null
    var results: Array<DistanceItem>? = null

    inner class CurrentCoords {
        var lat: String? = null
        val lng: String? = null
    }

    inner class DistanceItem {
        var busStopCode: String? = null
        val Latitude: Double = 0.toDouble()
        val Longitude: Double = 0.toDouble()
        val dist: Float = 0.toFloat()
    }


}

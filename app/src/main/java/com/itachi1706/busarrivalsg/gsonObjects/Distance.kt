package com.itachi1706.busarrivalsg.gsonObjects

/**
 * Created by Kenneth on 27/8/2018.
 * for com.itachi1706.busarrivalsg.gsonObjects in SingBuses
 */
class Distance (var currentCoord: CurrentCoords? = null, var results: Array<DistanceItem>? = null) {
    data class CurrentCoords (var lat: String? = null, val lng: String? = null)
    data class DistanceItem (var BusStopCode: String? = null, val Latitude: Double = 0.toDouble(), val Longitude: Double = 0.toDouble(), val dist: Float = 0.toFloat())
}

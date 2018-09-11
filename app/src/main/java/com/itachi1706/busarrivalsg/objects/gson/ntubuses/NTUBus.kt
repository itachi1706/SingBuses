@file:Suppress("PackageName")

package com.itachi1706.busarrivalsg.objects.gson.ntubuses

/**
 * Created by Kenneth on 7/9/2018.
 * for com.itachi1706.busarrivalsg.GsonObjects.ntubuses in SingBuses
 */
class NTUBus {
    val routes: Array<Route>? = null

    private fun convertStringToDouble(s: String): Double {
        return java.lang.Double.parseDouble(s)
    }

    private fun convertStringToLong(s: String): Long {
        return java.lang.Long.parseLong(s)
    }

    private fun convertLocationString(lonOrlat: String): Float {
        return java.lang.Float.parseFloat(lonOrlat)
    }

    inner class Route {

        val route: MapRouting? = null
        val vehicles: Array<Vehicles>? = null
        val name: String? = null
        val routename: String? = null
        val routenameraw: String? = null
        val routethrough: String? = null
        val id: Int = 0
    }

    inner class Vehicles {
        val device_ts: String? = null
        val lat: String? = null
        val lon: String? = null
        val registration_code: String? = null
        val speed: String? = null
        val ts: String? = null
        val license_no: String? = null
        val bearing: Int = 0
        val routevariant_id: Int = 0
        val vehicle_id: Int = 0
        val position: VehiclePosition? = null
        val stats: VehicleStats? = null

        fun getLatVal(): Double {
            if (lat != null) return convertStringToDouble(lat)
            return 0.0
        }

        fun getLonVal(): Double {
            if (lon != null) return convertStringToDouble(lon)
            return 0.0
        }
    }

    inner class VehiclePosition {
        val bearing: Int = 0
        val speed: Int = 0
        val device_ts: Long = 0
        val ts: Long = 0
        val lat: String? = null
        val lon: String? = null
    }

    inner class VehicleStats {
        val avg_speed: String? = null
        val cumm_speed_10: String? = null
        val cumm_speed_2: String? = null
        val lat: String? = null
        val lon: String? = null
        val bearing: Int = 0
        val speed: Int = 0
        val device_ts: Long = 0
        val ts: Long = 0
    }

    inner class MapRouting {
        val center: Array<MapPoints>? = null
        val id: Int = 0
        val nodes: Array<MapNodes>? = null
    }

    inner class MapNodes {
        val id: Int = 0
        val lat: Float = 0.toFloat()
        val lon: Float = 0.toFloat()
        val name: String? = null
        val short_direction: String? = null
        val is_stop_point: Boolean = false
        val points: Array<MapPoints>? = null
    }

    inner class MapPoints {
        val lat: Float = 0.toFloat()
        val lon: Float = 0.toFloat()
    }
}

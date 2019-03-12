package com.itachi1706.busarrivalsg.objects.gson.ntubuses

/**
 * Created by Kenneth on 12/3/2019.
 * for com.itachi1706.busarrivalsg.objects.gson.ntubuses in SingBuses
 */
class NTUBusTimings {

    val id: Int = 0
    val name: String = "Unknown Stop"
    val forecast: Array<Forecast>? = null

    inner class Forecast {
        val forecast_seconds: Double = 0.0
        val rv_id: Int = 0
        val total_pass: Double = 0.0
        val vehicle: String = ""
        val vehicle_id: Int = 0
        val route: Route? = null

        inner class Route {
            val id: Int = 0
            val name: String = "Unknown Route"
            val short_name: String = "Unknown Route"
        }
    }
}
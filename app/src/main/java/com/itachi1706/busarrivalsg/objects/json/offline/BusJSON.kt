package com.itachi1706.busarrivalsg.objects.json.offline

import kotlinx.serialization.Serializable

@Serializable
data class BusJSON(
    val service: String? = null,
    val operator: String? = null,
    val stop: String? = null,
    val stopName: String? = null
) {
    override fun toString(): String {
        return "BusObject{service=$service, operator=$operator,stop=$stop,stopName=$stopName}"
    }
}

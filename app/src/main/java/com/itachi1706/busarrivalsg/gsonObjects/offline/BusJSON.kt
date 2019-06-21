package com.itachi1706.busarrivalsg.gsonObjects.offline

/**
 * Created by Kenneth on 18/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.gsonObjects.offline
 */
data class BusJSON (val service: String? = null, val operator: String? = null, val stop: String? = null, val stopName: String? = null) {
    override fun toString(): String { return "BusObject{service=$service, operator=$operator,stop=$stop,stopName=$stopName}"
    }
}

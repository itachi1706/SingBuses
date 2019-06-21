package com.itachi1706.busarrivalsg.objects

/**
 * Created by Kenneth on 17/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg
 */
data class BusServices(var serviceNo: String = "", var operator: String = "", var stopID: String = "", var currentBus: BusStatus? = null, var nextBus: BusStatus? = null,
    var isObtainedNextData: Boolean = false, var time: Long = 0, var stopName: String = "", var isSvcStatus: Boolean = false, var subsequentBus: BusStatus? = null)

package com.itachi1706.busarrivalsg.Objects

/**
 * Created by Kenneth on 17/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg
 */
class BusServices {

    var serviceNo: String? = null
    var operator: String? = null
    var stopID: String? = null
    var currentBus: BusStatus? = null
    var nextBus: BusStatus? = null
    var isObtainedNextData: Boolean = false
    var time: Long = 0
    var stopName: String? = null

    var isSvcStatus: Boolean = false

    //Going to be implemented from 12 November 2016
    var subsequentBus: BusStatus? = null
}

package com.itachi1706.busarrivalsg.util

import com.itachi1706.helperlib.helpers.LogHelper.e
import com.itachi1706.helperlib.helpers.LogHelper.i

/**
 * Created by Kenneth on 7/10/2016.
 * for com.itachi1706.busarrivalsg.Util in SingBuses
 */
class Timings(private val tag: String, private val verbose: Boolean) {
    private var start: Long = -999
    private var end: Long = -999
    var duration: Long = -999
        private set

    fun start() {
        this.start = System.currentTimeMillis()
        if (this.verbose) i(tag, "Started timing on $start")
    }

    fun end(): Boolean {
        if (start == -999L) {
            e(tag, "Cannot end timing without starting it!")
            return false
        }
        this.end = System.currentTimeMillis()
        this.duration = this.end - this.start
        if (this.verbose) i(tag, "Ended timing on $end")

        i(tag, "Process finished in $duration ms")
        return true
    }

    fun reset() {
        this.start = -999
        this.end = -999
        this.duration = -999
    }
}

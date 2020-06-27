package com.itachi1706.busarrivalsg.util;

import com.itachi1706.helperlib.helpers.LogHelper;

/**
 * Created by Kenneth on 7/10/2016.
 * for com.itachi1706.busarrivalsg.Util in SingBuses
 */

public class Timings {

    private final String tag;
    private final boolean verbose;
    private long start = -999, end = -999, duration = -999;

    public Timings(String tag, boolean verbose) {
        this.verbose = verbose;
        this.tag = tag;
    }

    public void start() {
        this.start = System.currentTimeMillis();
        if (this.verbose) LogHelper.i(tag, "Started timing on " + start);
    }

    public boolean end() {
        if (start == -999) {
            LogHelper.e(tag, "Cannot end timing without starting it!");
            return false;
        }
        this.end = System.currentTimeMillis();
        this.duration = this.end - this.start;
        if (this.verbose) LogHelper.i(tag, "Ended timing on " + end);

        LogHelper.i(tag, "Process finished in " + this.duration + " ms");
        return true;
    }

    public long getDuration() {
        return this.duration;
    }

    public void reset() {
        this.start = -999;
        this.end = -999;
        this.duration = -999;
    }
}

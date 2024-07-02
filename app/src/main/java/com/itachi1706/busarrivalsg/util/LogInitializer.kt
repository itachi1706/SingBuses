package com.itachi1706.busarrivalsg.util

import com.google.firebase.crashlytics.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.itachi1706.helperlib.helpers.LogHelper
import com.itachi1706.helperlib.interfaces.LogHandler

/**
 * Created by Kenneth on 27/6/2020.
 * for com.itachi1706.busarrivalsg.util in SingBuses
 */
object LogInitializer {
    @JvmStatic
    fun initLogger() {
        if (!BuildConfig.DEBUG) LogHelper.addExternalLog(object : LogHandler {
            override fun handleExtraLogging(logLevel: Int, tag: String, message: String) {
                val crashlytics = FirebaseCrashlytics.getInstance()
                crashlytics.log(LogHelper.getGenericLogString(logLevel, tag, message))
            }
        })
    }
}
package com.itachi1706.busarrivalsg.util

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.itachi1706.busarrivalsg.R
import com.itachi1706.busarrivalsg.gsonObjects.sgLTA.BusStopJSON
import com.itachi1706.helperlib.helpers.LogHelper

class ShortcutHelper(val context: Context) {

    companion object {
        const val TAG = "ShortcutHelper"
    }


    fun getInfo() {
        LogHelper.d(
            TAG,
            "Count: Max: ${getShortcutCount()}, Dynamic: ${getDynamicShortcutCount()}, Static: ${getStaticShortcutCount()}"
        )
    }

    fun updateBusStopShortcuts(busStop: BusStopJSON, intent: Intent): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            LogHelper.w(TAG, "Not running on Android Nougat or higher, skipping shortcut creation")
            return false
        }

        getInfo()
        intent.setAction(Intent.ACTION_VIEW)

        val shortcut = ShortcutInfoCompat.Builder(context, "bus-" + busStop.BusStopCode)
            .setShortLabel(busStop.Description)
            .setLongLabel(busStop.Description)
            .setIntent(intent)
            .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher_round))
            .build()

        LogHelper.i(
            TAG,
            "Pushing Shortcut for Bus Stop ${busStop.BusStopCode} (${busStop.Description})"
        )

        return pushShortcut(shortcut)
    }

    private fun pushShortcut(shortcutInfo: ShortcutInfoCompat): Boolean {
        return ShortcutManagerCompat.pushDynamicShortcut(context, shortcutInfo)
    }

    private fun getShortcutCount(): Int {
        return ShortcutManagerCompat.getMaxShortcutCountPerActivity(context)
    }

    private fun getDynamicShortcutCount(): Int {
        return ShortcutManagerCompat.getDynamicShortcuts(context).size
    }

    private fun getStaticShortcutCount(): Int {
        return ShortcutManagerCompat.getShortcuts(
            context,
            ShortcutManagerCompat.FLAG_MATCH_MANIFEST
        ).size
    }
}
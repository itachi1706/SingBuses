package com.itachi1706.busarrivalsg.tasks

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.itachi1706.busarrivalsg.Database.BusStopsDB
import com.itachi1706.busarrivalsg.R
import com.itachi1706.busarrivalsg.gsonObjects.sgLTA.BusStopJSONArray
import com.itachi1706.busarrivalsg.util.StaticVariables
import com.itachi1706.busarrivalsg.util.Timings
import com.itachi1706.helperlib.helpers.LogHelper
import com.itachi1706.helperlib.helpers.LogHelper.d
import com.itachi1706.helperlib.helpers.URLHelper
import java.io.IOException

class UpdateDatabase : Service() {

    companion object {
        const val TAG = "UpdateDatabase"
        const val NOTIFICATION_CHANNEL_ID = "tasks"
        const val UPDATE_URL = "https://api.itachi1706.com/api/busstops.php?api=2"
        const val MAX_RETRY = 5
    }

    private val sp by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        LogHelper.i(TAG, "Starting Database Update as Foreground Service")
        startForeground()

        refreshDatabase()

        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotification() {
        val notificationManager = NotificationManagerCompat.from(this)

        // create the notification channel
        val channel = NotificationChannelCompat.Builder(
            NOTIFICATION_CHANNEL_ID,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                NotificationManager.IMPORTANCE_DEFAULT
            else 0
        ).setName("Tasks").build()

        notificationManager.createNotificationChannel(channel)
    }

    private fun startForeground() {
        // Start Foreground Service
        createNotification()

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Updating Database")
            .setContentText("Updating Bus Database")
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        // promote to foreground service
        ServiceCompat.startForeground(
            this,
            1,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE
            else
                0
        )
    }

    private fun refreshDatabase() {
        sp.edit().putBoolean("busDBLoaded", false).apply()

        // Get data from API
        var retry = 0
        var dataObjects: BusStopJSONArray? = null
        while (retry < MAX_RETRY) {
            LogHelper.i(TAG, "Attempting to get data from API")
            val data = getFromApi()

            if (data != null) {
                if (data == "retry") {
                    LogHelper.e(TAG, "Error in API Call. Retrying...")
                    continue
                }

                dataObjects = parseJsonString(data)

                if (dataObjects != null) {
                    LogHelper.i(TAG, "Database data retrieved Successfully")
                    sp.edit().putBoolean("busDBLoaded", true).apply()
                    break
                }

                LogHelper.w(TAG, "Error in parsing JSON. Retrying...")
            }
            retry++
        }

        if (dataObjects == null) {
            LogHelper.e(TAG, "Failed to get data from API after 5 tries. Exiting...")
            Toast.makeText(this, "Failed to update database after 5 tries, try again later", Toast.LENGTH_LONG).show()
            stopSelf()
            return
        }

        processDatabase(dataObjects)
        stopSelf()
    }

    private fun parseJsonString(data: String): BusStopJSONArray? {
        val gson = Gson()
        if (!StaticVariables.checkIfYouGotJsonString(data)) {
            // Retry, invalid string
            LogHelper.w(TAG, "Invalid JSON String. Retrying...")
            return null
        }

        val replyArr = gson.fromJson(data, BusStopJSONArray::class.java)
        if (replyArr?.value == null) {
            LogHelper.e(TAG, "Error in parsing JSON String")
            return null
        }

        return replyArr
    }

    private fun processDatabase(dataObjects: BusStopJSONArray) {
        // Process the data here
        val db = BusStopsDB(this)

        val t1 = Timings(TAG, true)
        t1.start()
        val deletedRows = db.truncateDB()
        t1.end()
        d(TAG, "Deleted $deletedRows rows from the database")

        val t2 = Timings(TAG, true)
        t2.start()
        val data = dataObjects.value
        db.addMultipleToDB(data)
        t2.end()
        
        val count = db.size
        Toast.makeText(
            this,
            this.getString(R.string.toast_bus_stop_data_parse_success, count),
            Toast.LENGTH_SHORT
        ).show()
        d("GET-STOPS", "Loaded $count bus stops into the database")
        sp.edit().putBoolean("busDBLoaded", true).apply()
        sp.edit().putLong("busDBTimeUpdated", System.currentTimeMillis()).apply()
    }

    private fun getFromApi(): String? {
        // Do API Calls here
        val urlHelper = URLHelper(UPDATE_URL)
        var tmp: String?
        try {
            tmp = urlHelper.executeString()
        } catch (err: IOException) {
            LogHelper.e(TAG, "Error in API Call: ${err.message}", err)
            tmp = "retry"
        }

        return tmp
    }

    override fun onDestroy() {
        LogHelper.i(TAG, "Destroying Service")
        super.onDestroy()
    }

    override fun onTimeout(startId: Int) {
        LogHelper.w(TAG, "Service Timeout. Stopping Service")
        stopSelf()
        super.onTimeout(startId)
    }
}
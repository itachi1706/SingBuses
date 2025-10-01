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
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.itachi1706.busarrivalsg.R
import com.itachi1706.busarrivalsg.database.BusStopsDb
import com.itachi1706.busarrivalsg.objects.json.ltasg.BusStopJSON
import com.itachi1706.busarrivalsg.util.Timings
import com.itachi1706.helperlib.helpers.ApiCallsHelper
import com.itachi1706.helperlib.helpers.LogHelper
import com.itachi1706.helperlib.helpers.LogHelper.d
import com.itachi1706.helperlib.objects.ApiResponse
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class UpdateDatabaseTask : Service(), ApiCallsHelper.ApiCallListener {

    companion object {
        const val TAG = "UpdateDatabase"
        const val NOTIFICATION_CHANNEL_ID = "tasks"
        const val UPDATE_PATH = "sg-buses/stops"
        const val MAX_RETRY = 5
    }

    private var retry = 0

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
        sp.edit { putBoolean("busDBLoaded", false) }

        // Get data from API
        if (retry < MAX_RETRY) {
            retry++
            LogHelper.i(TAG, "Attempting to get data from API. Try #$retry/$MAX_RETRY")
            ApiCallsHelper(this).makeGetCall(UPDATE_PATH, this)
        } else {
            LogHelper.e(TAG, "Failed to get data from API after 5 tries. Exiting...")
            Toast.makeText(
                this,
                "Failed to update database after 5 tries, try again later",
                Toast.LENGTH_LONG
            ).show()
            stopSelf()
        }
    }

    override fun onApiCallSuccess(response: ApiResponse) {
        d(TAG, "onApiCallSuccess: $response")
        if (!response.success || response.data == null) {
            LogHelper.e(TAG, "API Call was not successful, retrying. Error Message: ${response.error}")
            refreshDatabase() // Retry
            return
        }

        val json = Json { ignoreUnknownKeys = true }
        val data = try {
            response.getTypedData<Array<BusStopJSON>>(json)
        } catch (e: SerializationException) {
            LogHelper.e(TAG, "Serialization Exception: ${e.message}")
            null
        }
        if (!data.isNullOrEmpty()) {
            LogHelper.i(TAG, "Database data retrieved Successfully")
            sp.edit { putBoolean("busDBLoaded", true) }
            processDatabase(data)

            LogHelper.i(TAG, "Database Update Complete, stopping service")
            stopSelf()
        } else {
            LogHelper.w(TAG, "Error in parsing data. Retrying...")
            refreshDatabase()
        }
    }

    override fun onApiCallError(error: String) {
        LogHelper.e(TAG, "Error making API Call: $error")
        refreshDatabase() // Retry
    }

    private fun processDatabase(data: Array<BusStopJSON>) {
        // Process the data here
        val db = BusStopsDb(this)

        val t1 = Timings(TAG, true)
        t1.start()
        val deletedRows = db.truncateDb()
        t1.end()
        d(TAG, "Deleted $deletedRows rows from the database")

        val t2 = Timings(TAG, true)
        t2.start()
        db.addMultipleToDb(data)
        t2.end()

        val count = db.size
        Toast.makeText(
            this,
            this.getString(R.string.toast_bus_stop_data_parse_success, count),
            Toast.LENGTH_SHORT
        ).show()
        d("GET-STOPS", "Loaded $count bus stops into the database")
        sp.edit {
            putBoolean("busDBLoaded", true)
            putLong("busDBTimeUpdated", System.currentTimeMillis())
        }
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
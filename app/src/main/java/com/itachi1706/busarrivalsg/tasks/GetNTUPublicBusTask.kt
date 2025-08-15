package com.itachi1706.busarrivalsg.tasks

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.itachi1706.busarrivalsg.NTUBusActivity
import com.itachi1706.busarrivalsg.R
import com.itachi1706.busarrivalsg.database.BusStopsDB
import com.itachi1706.busarrivalsg.objects.gson.ltasg.BusStopJSON
import com.itachi1706.busarrivalsg.util.StaticVariables
import com.itachi1706.helperlib.concurrent.CoroutineAsyncTask
import com.itachi1706.helperlib.helpers.LogHelper.d
import com.itachi1706.helperlib.helpers.LogHelper.e
import com.itachi1706.helperlib.helpers.LogHelper.i
import com.itachi1706.helperlib.helpers.LogHelper.w
import com.itachi1706.helperlib.helpers.URLHelper
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.SocketTimeoutException
import java.net.URLEncoder

class GetNTUPublicBusTask(activity: Activity, private val update: Boolean) :
    CoroutineAsyncTask<Unit, Unit, Int>(TASK_NAME) {
    private var actRef: WeakReference<Activity> = WeakReference(activity)
    private var exception: Exception? = null

    companion object {
        private const val TAG = "GetNTUPublicBusTask"
        private val TASK_NAME = GetNTUPublicBusTask::class.simpleName ?: "GetNTUPublicBusTask"

        private const val BUS_CSV = "27199:199;27261:179;27261:179A"
        private val URL = "https://api.itachi1706.com/v1/sg-buses/arrivals?csv=${
            URLEncoder.encode(
                BUS_CSV,
                "utf-8"
            )
        }"
    }

    // Bus Stop Codes to get (we will get it based off the last stop on campus)
    // - 27199 (199)
    // - 27261 (179[A])
    override fun doInBackground(vararg params: Unit?): Int {
        val mActivity = actRef.get()
        if (mActivity == null) {
            w(TAG, "Activity is null, cannot proceed with task")
            return 3
        }
        d(TAG, URL)

        var tmp: String?
        try {
            val start = System.currentTimeMillis()
            val urlHelper = URLHelper(URL)
            tmp = urlHelper.executeString()
            i(TAG, "Data retrieved in " + (System.currentTimeMillis() - start) + "ms")
        } catch (e: IOException) {
            exception = e
            return 1
        }

        d(TAG, tmp)
        if (!StaticVariables.checkIfYouGotJsonString(tmp)) {
            exception =
                Exception(mActivity.resources?.getString(R.string.toast_message_invalid_json))
            return 2
        }

        val sendForMapParsingIntent = Intent(NTUBusActivity.RECEIVE_NTU_PUBLIC_BUS_DATA_EVENT)
        sendForMapParsingIntent.putExtra("data", tmp)
        sendForMapParsingIntent.putExtra("update", true)
        mActivity.runOnUiThread {
            LocalBroadcastManager.getInstance(mActivity).sendBroadcast(sendForMapParsingIntent)
        }

        val sp = PreferenceManager.getDefaultSharedPreferences(mActivity)
        if (!update && sp.getBoolean("showntusbsstops", true)) {
            // Send data related to the bus stops as well
            BusStopsDB(mActivity).use { db ->
                d(TAG, "Fetching bus stops from DB for NTU Public Buses")
                val jsons = mutableListOf<BusStopJSON>()
                jsons.addAll(db.getBusStopsBySvcNo("179", "SMRT"))
                jsons.addAll(db.getBusStopsBySvcNo("179A", "SMRT"))
                jsons.addAll(db.getBusStopsBySvcNo("199", "SMRT"))
                val stops = jsons.toTypedArray()

                val gson = Gson()
                val js = gson.toJson(stops, Array<BusStopJSON>::class.java)
                val sendForParseIntent = Intent(NTUBusActivity.RECEIVE_NTU_PUBLIC_BUS_DATA_EVENT)
                sendForParseIntent.putExtra("data", js)
                sendForParseIntent.putExtra("update", false)
                mActivity.runOnUiThread {
                    LocalBroadcastManager.getInstance(mActivity).sendBroadcast(sendForParseIntent)
                }
            }
        }

        return 0
    }

    override fun onPostExecute(result: Int?) {
        val context = actRef.get()
        if (exception != null && result != 0) {
            e(TAG, "Exception occurred (${exception?.message})")
            if (exception is SocketTimeoutException) {
                Toast.makeText(
                    context,
                    "NTU API did not respond in a timely manner",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(context, exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
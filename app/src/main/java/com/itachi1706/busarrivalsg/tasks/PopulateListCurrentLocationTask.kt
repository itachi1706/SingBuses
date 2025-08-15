package com.itachi1706.busarrivalsg.tasks

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.itachi1706.busarrivalsg.R
import com.itachi1706.busarrivalsg.database.BusStopsDB
import com.itachi1706.busarrivalsg.fragments.BusStopNearbyFragment
import com.itachi1706.busarrivalsg.objects.gson.Distance
import com.itachi1706.busarrivalsg.objects.gson.ltasg.BusStopJSON
import com.itachi1706.busarrivalsg.recyclerviews.BusStopRecyclerAdapter
import com.itachi1706.busarrivalsg.util.StaticVariables
import com.itachi1706.helperlib.concurrent.CoroutineAsyncTask
import com.itachi1706.helperlib.helpers.LogHelper.d
import com.itachi1706.helperlib.helpers.LogHelper.e
import com.itachi1706.helperlib.helpers.URLHelper
import com.itachi1706.helperlib.helpers.ValidationHelper
import com.itachi1706.helperlib.objects.ApiResponse
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.SocketTimeoutException
import java.net.URLEncoder

class PopulateListCurrentLocationTask(
    activity: Activity,
    private val db: BusStopsDB,
    private val adapter: BusStopRecyclerAdapter
) : CoroutineAsyncTask<Location, Unit, Int>(TASK_NAME) {
    private var contextRef: WeakReference<Activity> = WeakReference(activity)
    private var exception: Exception? = null

    companion object {
        private const val TAG = "PopulateListCurrentLocationTask"
        private val TASK_NAME =
            PopulateListCurrentLocationTask::class.simpleName ?: "PopulateListCurrentLocationTask"
    }

    override fun doInBackground(vararg params: Location?): Int {
        val location = params.firstOrNull() ?: return -1
        val context = contextRef.get() ?: return -1
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val limit = sp.getString("nearbyStopsCount", "20")?.toIntOrNull() ?: 20

        // Validation stuff
        // NOTE: Switch to header based validation for new API. This is temporary for backwards compatibility
        val signature = URLEncoder.encode(ValidationHelper.getSignatureForValidation(context), "utf-8")
        var url =
            "https://api.itachi1706.com/v1/sg-buses/nearest?lat=" + location.latitude + "&lng=" + location.longitude + "&limit=" + limit
        d(TAG, "URL: $url")
        url += "&sig=$signature&package=${context.packageName}"
        var tmp: String?
        try {
            val urlHelper = URLHelper(url)
            tmp = urlHelper.executeString()
        } catch (e: IOException) {
            exception = e
            return 1
        }

        // Do processing
        val gson = Gson()
        d(TAG, tmp)
        if (!StaticVariables.checkIfYouGotJsonString(tmp)) {
            exception = Exception(context.resources.getString(R.string.toast_message_invalid_json))
            return 2
        }

        val tmp2 = gson.fromJson(tmp, ApiResponse::class.java)
        val tmp3 = gson.toJson(tmp2.data)
        d(TAG, "Data: $tmp3")

        val distArray = gson.fromJson(tmp3, Distance::class.java)
        if (distArray == null || distArray.stops == null) {
            exception = Exception("Invalid distance retrieved from API. Please try again later")
            return 3
        }

        val results = distArray.stops
        val stops = mutableListOf<BusStopJSON>()
        for (map in results!!) {
            val distance = map.dist
            val stop = db.getBusStopByBusStopCode(map.busStopCode)
            stop.distance = distance * 1000 // Convert to metres, its currently in km
            stop.isHasDistance = true

            stops.add(stop)
        }

        val sendForMapParsingIntent = Intent(BusStopNearbyFragment.RECEIVE_NEARBY_STOPS_EVENT)
        val listType = object : TypeToken<ArrayList<BusStopJSON>>() {}.type
        sendForMapParsingIntent.putExtra("data", gson.toJson(stops, listType))
        context.runOnUiThread {
            LocalBroadcastManager.getInstance(context).sendBroadcast(sendForMapParsingIntent)
            adapter.updateAdapter(stops)
            adapter.notifyItemRangeChanged(0, stops.size)
        }

        return 0
    }

    override fun onPostExecute(result: Int?) {
        val context = contextRef.get()
        if (exception != null && result != 0) {
            e(TAG, "Exception occurred (" + exception?.message + ")")
            if (exception is SocketTimeoutException) {
                Toast.makeText(
                    context,
                    R.string.toast_message_timeout_distance_api,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(context, exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
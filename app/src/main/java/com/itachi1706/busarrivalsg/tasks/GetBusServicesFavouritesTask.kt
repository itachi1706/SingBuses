package com.itachi1706.busarrivalsg.tasks

import android.app.Activity
import android.widget.Toast
import com.google.gson.Gson
import com.itachi1706.busarrivalsg.R
import com.itachi1706.busarrivalsg.objects.BusServices
import com.itachi1706.busarrivalsg.objects.BusStatus
import com.itachi1706.busarrivalsg.objects.gson.ltasg.BusArrivalMain
import com.itachi1706.busarrivalsg.recyclerviews.FavouritesRecyclerAdapter
import com.itachi1706.busarrivalsg.util.StaticVariables
import com.itachi1706.helperlib.concurrent.CoroutineAsyncTask
import com.itachi1706.helperlib.helpers.LogHelper
import com.itachi1706.helperlib.helpers.URLHelper
import com.itachi1706.helperlib.objects.ApiResponse
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.SocketTimeoutException
import java.net.URLEncoder

class GetBusServicesFavouritesTask(
    activity: Activity,
    private val adapter: FavouritesRecyclerAdapter
) : CoroutineAsyncTask<BusServices, Void, String>(TASK_NAME) {
    private var actRef: WeakReference<Activity> = WeakReference(activity)
    private var exception: Exception? = null

    private var busObj: Array<BusServices?> = arrayOfNulls(0)

    override fun doInBackground(vararg params: BusServices?): String {
        // Create the bus object string
        val sb = StringBuilder()
        for (s in params) {
            if (s == null) continue
            sb.append(s.stopID).append(":").append(s.serviceNo).append(";")
        }

        var csv = sb.toString()
        csv = csv.substring(0, csv.length - 1) // Remove the last semicolon
        busObj = arrayOf(*params)
        csv = URLEncoder.encode(csv, "utf-8") // Encode URL

        val url = "https://api.itachi1706.com/v1/sg-buses/arrivals?csv=$csv"
        var tmp = ""
        val urlHelper = URLHelper(url)

        LogHelper.d(TAG, "GET-FAV-BUS-SERVICE: $url")
        try {
            tmp = urlHelper.executeString()
        } catch (e: IOException) {
            exception = e
        }

        return tmp
    }

    override fun onPostExecute(result: String?) {
        if (result == null) {
            LogHelper.e(TAG, "onPostExecute: JSON is null")
            return
        }
        val activity = actRef.get()
        if (activity == null) return // NO-OP
        if (exception != null) {
            if (exception is SocketTimeoutException) {
                Toast.makeText(
                    activity,
                    R.string.toast_message_timeout_request_retry,
                    Toast.LENGTH_SHORT
                ).show()
                GetBusServicesFavouritesTask(activity, adapter).executeOnExecutor(*busObj)
            } else {
                Toast.makeText(activity, exception!!.message, Toast.LENGTH_SHORT).show()
            }
        } else {
            // Parse result
            val gson = Gson()
            if (!StaticVariables.checkIfYouGotJsonString(result)) {
                // Retry from invalid string

                //Invalid string, retrying
                Toast.makeText(
                    activity,
                    R.string.toast_message_invalid_json_retry,
                    Toast.LENGTH_SHORT
                ).show()
                GetBusServicesFavouritesTask(activity, adapter).executeOnExecutor(*busObj)
                return
            }

            LogHelper.d(TAG, "GET-FAV-BUS-SERVICE: JSON Result: $result")
            val resp = gson.fromJson(result, ApiResponse::class.java)
            val result2 = gson.toJson(resp.data)
            val mainArrs = gson.fromJson(result2, Array<BusArrivalMain>::class.java)

            var jsonError = false
            if (mainArrs == null || mainArrs.isEmpty() || (mainArrs[0].services == null)) {
                jsonError = true
            }

            if (jsonError) {
                LogHelper.e(TAG, "FAV-GET: Retrying...")
                GetBusServicesFavouritesTask(activity, adapter).executeOnExecutor(*busObj)
                return
            }

            for (mainArr in mainArrs) {
                val array = mainArr.services

                // Assuming one
                val item = array!![0]
                val busObjs: BusServices? = busObj.filterNotNull().find { it.serviceNo.equals(item.serviceNo, ignoreCase = true) && it.stopID.equals(mainArr.busStopCode, ignoreCase = true) }

                if (busObjs == null) {
                    LogHelper.e(TAG, "GET-FAV-BUS-SERVICE: Cannot find bus object. Something went wrong!")
                    continue
                }

                val nextBus = BusStatus(
                    estimatedArrival = item.nextBus!!.estimatedArrival,
                    visitNumber = item.nextBus.visitNumberD,
                    latitude = item.nextBus.latitudeD,
                    longitude = item.nextBus.longitudeD,
                    terminatingID = item.nextBus.destinationCode,
                    originatingID = item.nextBus.originCode
                )
                nextBus.setIsWheelChairAccessible(item.nextBus.feature)
                nextBus.setLoad(item.nextBus.load)
                nextBus.setBusType(item.nextBus.type)

                val subsequentBus = BusStatus(
                    estimatedArrival = item.nextBus2!!.estimatedArrival,
                    visitNumber = item.nextBus2.visitNumberD,
                    latitude = item.nextBus2.latitudeD,
                    longitude = item.nextBus2.longitudeD,
                    terminatingID = item.nextBus2.destinationCode,
                    originatingID = item.nextBus2.originCode
                )
                subsequentBus.setIsWheelChairAccessible(item.nextBus2.feature)
                subsequentBus.setLoad(item.nextBus2.load)
                subsequentBus.setBusType(item.nextBus2.type)

                val subsequent2Bus = BusStatus(
                    estimatedArrival = item.nextBus3!!.estimatedArrival,
                    visitNumber = item.nextBus3.visitNumberD,
                    latitude = item.nextBus3.latitudeD,
                    longitude = item.nextBus3.longitudeD,
                    terminatingID = item.nextBus3.destinationCode,
                    originatingID = item.nextBus3.originCode
                )
                subsequent2Bus.setIsWheelChairAccessible(item.nextBus3.feature)
                subsequent2Bus.setLoad(item.nextBus3.load)
                subsequent2Bus.setBusType(item.nextBus3.type)

                busObjs.currentBus = nextBus
                busObjs.nextBus = subsequentBus
                busObjs.subsequentBus = subsequent2Bus
                busObjs.time = System.currentTimeMillis()
                busObjs.isSvcStatus = checkServiceOperational(nextBus, subsequentBus, subsequent2Bus)
                busObjs.isObtainedNextData = true

                // Go through list and update the item
                for (i in 0 until StaticVariables.favouritesList.size) {
                    val ob = StaticVariables.favouritesList[i]
                    if (ob.serviceNo == busObjs.serviceNo && ob.stopID == busObjs.stopID) {
                        // Update item in list
                        StaticVariables.favouritesList[i] = busObjs
                        adapter.updateAdapter(StaticVariables.favouritesList, mainArr.currentTime)
                        adapter.notifyItemChanged(i)
                        LogHelper.d(TAG, "GET-FAV-BUS-SERVICE: Updated item at position $i")
                        break
                    }
                }
            }
        }
    }

    private fun checkServiceOperational(one: BusStatus, two: BusStatus, three: BusStatus): Boolean {
        return !(one.estimatedArrival == null && two.estimatedArrival == null && three.estimatedArrival == null)
                && !(one.estimatedArrival.isNullOrEmpty() && two.estimatedArrival.isNullOrEmpty() && three.estimatedArrival.isNullOrEmpty())
    }

    companion object {
        private val TASK_NAME =
            GetBusServicesFavouritesTask::class.simpleName ?: "GetBusServicesFavouritesTask"
        private const val TAG = "GetBusServicesFavouritesTask"
    }
}
package com.itachi1706.busarrivalsg.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.itachi1706.busarrivalsg.objects.BusServices
import com.itachi1706.helperlib.helpers.LogHelper
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Created by Kenneth on 20/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg
 */
object StaticVariables {

    const val BASE_SERVER_URL = "https://api.itachi1706.com/v1"

    const val CUR = 0
    const val NEXT = 1
    const val SUB = 2

    private const val USE_SERVER_TIME = "useServerTime"

    var favouritesList = ArrayList<BusServices>()

    // HANDLER MESSAGES
    const val BUS_SERVICE_JSON_RETRIEVED = 101

    @SuppressLint("CheckResult")
    fun checkIfYouGotJsonString(jsonString: String?): Boolean {
        if (jsonString == null) return false
        return try {
            Json.parseToJsonElement(jsonString)
            true
        } catch (_: SerializationException) {
            false
        }
    }

    fun useServerTime(sp: SharedPreferences): Boolean {
        return sp.getBoolean(USE_SERVER_TIME, false)
    }

    fun parseLTAEstimateArrival(
        arrivalString: String,
        useServerTime: Boolean,
        serverTime: String?
    ): Long {
        return if (arrivalString.equals("", ignoreCase = true)) -9999 else parseEstimateArrival(
            arrivalString,
            useServerTime,
            serverTime
        )
    }

    fun checkBusLocationValid(lat: Double, lng: Double): Boolean {
        return !(lng == -1000.0 || lat == -1000.0) && !(lng == -11.0 && lat == -11.0) && !(lat == 0.0 && lng == 0.0)
    }

    private fun parseEstimateArrival(
        arrivalString: String,
        useServerTime: Boolean,
        serverTime: String?
    ): Long {
        val currentDate: Calendar
        if (!useServerTime || serverTime == null) {
            LogHelper.d("DATE", "Current Time Millis: " + System.currentTimeMillis())
            currentDate = Calendar.getInstance()
            currentDate.add(Calendar.MONTH, 1)
        } else {
            currentDate = parseDate(serverTime)
        }


        val arrivalDate = parseDate(arrivalString)

        LogHelper.d("COMPARE", "Current: $currentDate")
        LogHelper.d("COMPARE", "Arrival: $arrivalDate")
        val difference = arrivalDate.timeInMillis - currentDate.timeInMillis
        return TimeUnit.MILLISECONDS.toMinutes(difference)
    }

    private fun parseDate(dateString: String): Calendar {
        LogHelper.d("SPLIT", "Date String to parse: $dateString")
        val firstSplit =
            dateString.split("T".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val date = firstSplit[0]
        val time = firstSplit[1]
        val timeSplit = time.split("\\+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val trueTime = timeSplit[0]

        val dateSplit = date.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val year = Integer.parseInt(dateSplit[0])
        val month = Integer.parseInt(dateSplit[1])
        val dates = Integer.parseInt(dateSplit[2])

        val trueTimeSplit =
            trueTime.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val hr = Integer.parseInt(trueTimeSplit[0])
        val min = Integer.parseInt(trueTimeSplit[1])
        val sec = Integer.parseInt(trueTimeSplit[2])

        return GregorianCalendar(year, month, dates, hr, min, sec)
    }

    fun convertDateToString(date: Date): String {
        val df = SimpleDateFormat("EE dd MMM yyyy HH:mm:ss zz", Locale.US)
        return df.format(date)
    }

    fun checkIfCoarseLocationGranted(result: Map<String, Boolean>): Boolean {
        return result[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
    }
}

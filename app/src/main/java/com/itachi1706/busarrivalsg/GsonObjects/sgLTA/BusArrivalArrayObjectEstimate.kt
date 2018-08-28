@file:Suppress("PropertyName", "PrivatePropertyName")

package com.itachi1706.busarrivalsg.gsonObjects.sgLTA

import com.itachi1706.busarrivalsg.Util.BusesUtil

/**
 * Created by Kenneth on 18/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.GsonObjects.LTA
 */
class BusArrivalArrayObjectEstimate {
    val EstimatedArrival: String? = null
    val Load: String = ""
    val Feature: String = ""

    // Implemented from 12 November 2016
    val Latitude = "-11"
    val Longitude = "-11" //return double option
    private val VisitNumber: String = "0" //return int option

    // Implemented from 30 July 2017
    val OriginCode: String? = null
    val DestinationCode: String? = null
    val Type: String = ""

    val loadInt: Int
        get() = BusesUtil.getLoad(Load)

    val latitudeD: Double
        get() {
            try {
                return java.lang.Double.parseDouble(Latitude)
            } catch (e: NumberFormatException) {
                return -11.0
            }

        }

    val longitudeD: Double
        get() {
            try {
                return java.lang.Double.parseDouble(Longitude)
            } catch (e: NumberFormatException) {
                return -11.0
            }

        }

    val visitNumberD: Int
        get() {
            try {
                return Integer.parseInt(VisitNumber)
            } catch (e: NumberFormatException) {
                return 0
            }

        }

    val isWheelchairAccessible: Boolean
        get() = Feature != null && Feature.contains("WAB")

    val typeInt: Int
        get() = BusesUtil.getType(Type!!)

    fun hasLatitude(): Boolean {
        return Latitude.isNotEmpty()
    }

    fun hasLongitude(): Boolean {
        return Longitude.isNotEmpty()
    }
}

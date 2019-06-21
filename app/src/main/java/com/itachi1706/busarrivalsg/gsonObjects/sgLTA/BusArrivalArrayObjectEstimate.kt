@file:Suppress("PropertyName", "PrivatePropertyName")

package com.itachi1706.busarrivalsg.gsonObjects.sgLTA

import com.itachi1706.busarrivalsg.util.BusesUtil

/**
 * Created by Kenneth on 18/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.gsonObjects.sgLTA
 */
class BusArrivalArrayObjectEstimate (val EstimatedArrival: String? = null, val Load: String = "", val Feature: String = "", val Latitude: String = "-11", val Longitude: String = "-11",
        private val VisitNumber: String = "0", val OriginCode: String? = null, val DestinationCode: String? = null, val Type: String = "") {
    val loadInt: Int
        get() = BusesUtil.getLoad(Load)

    val latitudeD: Double
        get() {
            return try {
                java.lang.Double.parseDouble(Latitude)
            } catch (e: NumberFormatException) {
                -11.0
            }

        }

    val longitudeD: Double
        get() {
            return try {
                java.lang.Double.parseDouble(Longitude)
            } catch (e: NumberFormatException) {
                -11.0
            }

        }

    val visitNumberD: Int
        get() {
            return try {
                Integer.parseInt(VisitNumber)
            } catch (e: NumberFormatException) {
                0
            }

        }

    val isWheelchairAccessible: Boolean
        get() = Feature.contains("WAB")

    val typeInt: Int
        get() = BusesUtil.getType(Type)
}

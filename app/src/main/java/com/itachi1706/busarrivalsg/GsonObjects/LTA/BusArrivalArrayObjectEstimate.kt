package com.itachi1706.busarrivalsg.GsonObjects.LTA

import com.itachi1706.busarrivalsg.Objects.CommonEnums

/**
 * Created by Kenneth on 18/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.GsonObjects.LTA
 */
class BusArrivalArrayObjectEstimate {
    val estimatedArrival: String? = null
    private val Load: String? = null
    val feature: String? = null

    // Implemented from 12 November 2016
    val latitudeString = "-11"
    val longitudeString = "-11" //return double option
    val visitNumberString: String? = null //return int option

    // Implemented from 30 July 2017
    val originCode: String? = null
    val destinationCode: String? = null
    val type: String? = null

    val load: String
        get() = Load ?: ""

    val loadInt: Int
        get() {
            if (Load == null) return CommonEnums.UNKNOWN
            when (Load) {
                "SEA" -> return CommonEnums.BUS_SEATS_AVAIL
                "SDA" -> return CommonEnums.BUS_STANDING_AVAIL
                "LSD" -> return CommonEnums.BUS_LIMITED_SEATS
                else -> return CommonEnums.UNKNOWN
            }
        }

    val latitude: Double
        get() {
            try {
                return java.lang.Double.parseDouble(latitudeString)
            } catch (e: NumberFormatException) {
                return -11.0
            }

        }

    val longitude: Double
        get() {
            try {
                return java.lang.Double.parseDouble(longitudeString)
            } catch (e: NumberFormatException) {
                return -11.0
            }

        }

    val visitNumber: Int
        get() {
            try {
                return Integer.parseInt(visitNumberString)
            } catch (e: NumberFormatException) {
                return 0
            }

        }

    val isWheelchairAccessible: Boolean
        get() = feature != null && feature.contains("WAB")

    val typeInt: Int
        get() {
            when (type) {
                "SD" -> return CommonEnums.BUS_SINGLE_DECK
                "DD" -> return CommonEnums.BUS_DOUBLE_DECK
                "BD" -> return CommonEnums.BUS_BENDY
                else -> return CommonEnums.UNKNOWN
            }
        }

    fun hasLatitude(): Boolean {
        return latitudeString.length != 0
    }

    fun hasLongitude(): Boolean {
        return longitudeString.length != 0
    }
}

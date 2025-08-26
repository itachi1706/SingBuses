package com.itachi1706.busarrivalsg.objects

import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.itachi1706.busarrivalsg.R
import com.itachi1706.busarrivalsg.objects.gson.ltasg.BusArrivalArrayObjectEstimate
import com.itachi1706.busarrivalsg.util.BusesUtil
import com.itachi1706.busarrivalsg.util.StaticVariables

data class BusRecordView(
    val busService: TextView,
    val busOperator: TextView,
    val busArrival: Button,
    val busStatus: TextView,
    val busStopName: TextView,
    val wheelchairSupport: ImageView,
    val busType: TextView,
    val serverTime: Boolean
) {
    fun setInformationBusStopServices(
        busObj: BusArrivalArrayObjectEstimate?,
        currentTime: String,
        onClick: View.OnClickListener
    ) {
        if (busObj == null) {
            comingSoon(busArrival)
        } else if (busObj.estimatedArrival == null) {
            notArriving()
        } else {
            setCommonValid(
                busObj.estimatedArrival,
                currentTime,
                busObj.loadInt,
                busObj.isWheelchairAccessible,
                busObj.typeInt,
                onClick
            )
        }
    }

    fun setInformationFavourites(
        busObj: BusStatus?,
        currentTime: String,
        onClick: View.OnClickListener
    ) {
        if (busObj == null) {
            comingSoon(busArrival)
        } else if (busObj.estimatedArrival.isNullOrEmpty()) {
            notArriving()
        } else {
            setCommonValid(
                busObj.estimatedArrival!!,
                currentTime,
                busObj.load,
                busObj.isWheelChairAccessible,
                busObj.busType,
                onClick
            )
        }
    }

    private fun setCommonValid(
        estimatedArrival: String,
        currentTime: String,
        load: Int,
        isWheelchairAccessible: Boolean,
        type: Int,
        onClick: View.OnClickListener
    ) {
        val est = StaticVariables.parseLTAEstimateArrival(
            estimatedArrival,
            serverTime,
            currentTime
        )
        val arrivalStatus = when {
            est == -9999L -> {
                "-"
            }

            est <= 0 -> {
                "Arr"
            }

            else -> {
                est.toString()
            }
        }

        busArrival.text = arrivalStatus
        BusesUtil.applyColorLoad(busArrival, load)
        wheelchairSupport.visibility = View.INVISIBLE
        if (isWheelchairAccessible) {
            wheelchairSupport.visibility = View.VISIBLE
        }
        busType.visibility = View.INVISIBLE
        if (arrivalStatus != "-") {
            busType.text = BusesUtil.getType(type)
            busType.visibility = View.VISIBLE
        }
        busArrival.setOnClickListener(onClick)
    }

    private fun comingSoon(view: TextView) {
        view.setText(R.string.feature_coming_soon)
        view.setTextColor(Color.GRAY)
    }

    fun notArriving() {
        busArrival.text = "-"
        busArrival.setTextColor(Color.GRAY)
        wheelchairSupport.visibility = View.INVISIBLE
        busType.visibility = View.INVISIBLE
        busArrival.setOnClickListener(UnavailableButton())
    }

    fun processing() {
        busArrival.text = "..."
        busArrival.setTextColor(Color.GRAY)
    }

    inner class UnavailableButton() : View.OnClickListener {
        override fun onClick(v: View) {
            AlertDialog.Builder(v.context)
                .setTitle(R.string.dialog_title_bus_timing_unavailable)
                .setMessage(R.string.dialog_message_bus_timing_unavailable)
                .setPositiveButton(R.string.dialog_action_positive_close, null).show()
        }
    }
}
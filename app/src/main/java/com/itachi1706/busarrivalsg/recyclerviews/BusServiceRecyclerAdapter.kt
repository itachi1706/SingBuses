package com.itachi1706.busarrivalsg.recyclerviews

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.itachi1706.busarrivalsg.BusLocationMapsDialogFragment
import com.itachi1706.busarrivalsg.BusServicesAtStopRecyclerActivity
import com.itachi1706.busarrivalsg.R
import com.itachi1706.busarrivalsg.database.BusStopsDb
import com.itachi1706.busarrivalsg.databinding.RecyclerviewBusNumbersBinding
import com.itachi1706.busarrivalsg.objects.BusRecordView
import com.itachi1706.busarrivalsg.objects.BusServices
import com.itachi1706.busarrivalsg.objects.gson.ltasg.BusArrivalArrayObject
import com.itachi1706.busarrivalsg.util.BusesUtil
import com.itachi1706.busarrivalsg.util.StaticVariables
import com.itachi1706.helperlib.helpers.LogHelper.d
import com.itachi1706.helperlib.helpers.PrefHelper

class BusServiceRecyclerAdapter(
    private var items: MutableList<BusArrivalArrayObject>,
    private val activity: AppCompatActivity,
    private val serverTime: Boolean
) : RecyclerView.Adapter<BusServiceRecyclerAdapter.BusServiceViewHolder>() {

    private var currentTime: String = ""

    fun updateAdapter(newObjects: List<BusArrivalArrayObject>, currentTime: String) {
        items = newObjects.toMutableList()
        this.currentTime = currentTime
        notifyItemRangeChanged(0, items.size)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BusServiceViewHolder {
        val binding = RecyclerviewBusNumbersBinding.inflate(activity.layoutInflater, parent, false)

        return BusServiceViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: BusServiceViewHolder,
        position: Int
    ) {
        val i = items[position]

        holder.binding.tvBusOperator.text = i.operator
        holder.binding.tvBusOperator.setTextColor(BusesUtil.getOperatorColor(activity, i.operator))
        holder.binding.tvBusService.text = i.serviceNo

        val currentBusViews = BusRecordView(
            holder.binding.tvBusService,
            holder.binding.tvBusOperator,
            holder.binding.btnBusArrivalNow,
            holder.binding.tvBusStatus,
            holder.binding.tvBusStopName,
            holder.binding.ivWheelchairNow,
            holder.binding.tvBusTypeNow,
            serverTime
        )

        val nextBusViews = BusRecordView(
            holder.binding.tvBusService,
            holder.binding.tvBusOperator,
            holder.binding.btnBusArrivalNext,
            holder.binding.tvBusStatus,
            holder.binding.tvBusStopName,
            holder.binding.ivWheelchairNext,
            holder.binding.tvBusTypeNext,
            serverTime
        )

        val subBusViews = BusRecordView(
            holder.binding.tvBusService,
            holder.binding.tvBusOperator,
            holder.binding.btnBusArrivalSub,
            holder.binding.tvBusStatus,
            holder.binding.tvBusStopName,
            holder.binding.ivWheelchairSub,
            holder.binding.tvBusTypeSub,
            serverTime
        )

        if (!i.isSvcStatus) {
            holder.binding.tvBusStatus.text = activity.getString(R.string.service_not_operational)
            holder.binding.tvBusStatus.setTextColor(Color.RED)
            currentBusViews.notArriving()
            nextBusViews.notArriving()
            subBusViews.notArriving()
            return
        }
        holder.binding.tvBusStatus.text = activity.getString(R.string.service_operational)
        holder.binding.tvBusStatus.setTextColor(
            if (PrefHelper.isNightModeEnabled(activity)) Color.GREEN else ContextCompat.getColor(
                activity,
                R.color.dark_green
            )
        )

        // Current Bus
        currentBusViews.setInformationBusStopServices(
            i.nextBus,
            currentTime,
            ArrivalButton(i, i.stopCode, i.serviceNo, StaticVariables.CUR)
        )

        // 2nd Bus
        nextBusViews.setInformationBusStopServices(
            i.nextBus2,
            currentTime,
            ArrivalButton(i, i.stopCode, i.serviceNo, StaticVariables.NEXT)
        )

        // Subsequent Bus
        subBusViews.setInformationBusStopServices(
            i.nextBus3,
            currentTime,
            ArrivalButton(i, i.stopCode, i.serviceNo, StaticVariables.SUB)
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun getItem(position: Int): BusArrivalArrayObject? {
        return items[position]
    }

    inner class BusServiceViewHolder(val binding: RecyclerviewBusNumbersBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnLongClickListener {
        init {
            binding.root.setOnLongClickListener(this)
            binding.btnBusArrivalNow.setOnLongClickListener(this)
            binding.btnBusArrivalNext.setOnLongClickListener(this)
            binding.btnBusArrivalSub.setOnLongClickListener(this)

            binding.ivWheelchairNow.visibility = View.INVISIBLE
            binding.ivWheelchairNext.visibility = View.INVISIBLE
            binding.ivWheelchairSub.visibility = View.INVISIBLE
            binding.tvBusTypeNow.visibility = View.INVISIBLE
            binding.tvBusTypeNext.visibility = View.INVISIBLE
            binding.tvBusTypeSub.visibility = View.INVISIBLE
        }

        override fun onLongClick(v: View): Boolean {
            val position = this.layoutPosition
            val item = items[position]

            if (activity is BusServicesAtStopRecyclerActivity) {
                // Check based on thing and verify
                val fav = BusServices()
                fav.isObtainedNextData = false
                fav.operator = item.operator
                fav.serviceNo = item.serviceNo
                fav.stopID = item.stopCode
                d(
                    "Fav",
                    "Long Pressed on ${fav.serviceNo} at ${fav.stopName} with operator ${fav.operator}"
                )

                activity.favouriteOrUnfavourite(fav, item)
                return true
            }

            return false
        }

    }

    inner class ArrivalButton(
        busObj: BusArrivalArrayObject,
        busStopCode: String,
        svcNo: String,
        state: Int
    ) : View.OnClickListener {

        private val longitude: Double
        private val latitude: Double
        private val stopCode: String?
        private val serviceNo: String?
        private val busObj: BusArrivalArrayObject
        private val state: Int

        init {
            val status =
                if (state == StaticVariables.CUR) busObj.nextBus else if (state == StaticVariables.NEXT) busObj.nextBus2 else busObj.nextBus3
            this.state = state
            this.busObj = busObj
            this.longitude = status!!.longitudeD
            this.latitude = status.latitudeD
            this.stopCode = busStopCode.trim { it <= ' ' }
            this.serviceNo = svcNo.trim { it <= ' ' }
        }

        override fun onClick(v: View?) {
            if (longitude == -1000.0 || latitude == -1000.0) {
                //Error, invalid location
                AlertDialog.Builder(activity)
                    .setTitle(R.string.dialog_title_bus_location_unavailable)
                    .setMessage(R.string.dialog_message_bus_location_unavailable)
                    .setPositiveButton(R.string.dialog_action_positive_close, null).show()
                return
            }
            if (longitude == -11.0 && latitude == -11.0) {
                AlertDialog.Builder(activity).setTitle(R.string.dialog_title_bus_timing_unavailable)
                    .setMessage(R.string.dialog_message_bus_timing_unavailable)
                    .setPositiveButton(R.string.dialog_action_positive_close, null).show()
                return
            }

            if (latitude == 0.0 && longitude == 0.0) {
                AlertDialog.Builder(activity).setTitle("Bus Service in Depot")
                    .setMessage("The Bus Service is currently still in the depot so no location can be obtained!")
                    .setPositiveButton("Close", null).show()
                return
            }

            //Check if Google Play Services is enabled
            val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity)
            if (code != ConnectionResult.SUCCESS) {
                GoogleApiAvailability.getInstance().getErrorDialog(activity, code, 0)
                return
            }

            val mapsArgs = Bundle()
            mapsArgs.putString("busCode", stopCode)
            mapsArgs.putString("busSvcNo", serviceNo)

            // 3 Bus statuses
            mapsArgs.putDouble("lat1", busObj.nextBus!!.latitudeD)
            mapsArgs.putDouble("lng1", busObj.nextBus.longitudeD)
            mapsArgs.putString("arr1", busObj.nextBus.estimatedArrival)
            mapsArgs.putInt("type1", busObj.nextBus.typeInt)
            mapsArgs.putDouble("lat2", busObj.nextBus2!!.latitudeD)
            mapsArgs.putDouble("lng2", busObj.nextBus2.longitudeD)
            mapsArgs.putString("arr2", busObj.nextBus2.estimatedArrival)
            mapsArgs.putInt("type2", busObj.nextBus2.typeInt)
            mapsArgs.putDouble("lat3", busObj.nextBus3!!.latitudeD)
            mapsArgs.putDouble("lng3", busObj.nextBus3.longitudeD)
            mapsArgs.putString("arr3", busObj.nextBus3.estimatedArrival)
            mapsArgs.putInt("type3", busObj.nextBus3.typeInt)
            mapsArgs.putString("sTime", currentTime)
            mapsArgs.putInt("state", state)

            //Get Bus stop longitude and latitude
            val db = BusStopsDb(activity)
            val busStop = db.getBusStopByBusStopCode(stopCode)
            if (busStop != null) {
                mapsArgs.putDouble("buslat", busStop.latitude)
                mapsArgs.putDouble("buslng", busStop.longitude)
            }

            val dialog = BusLocationMapsDialogFragment()
            dialog.setArguments(mapsArgs)
            dialog.show(activity.supportFragmentManager, "123")
        }
    }
}
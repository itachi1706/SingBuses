package com.itachi1706.busarrivalsg.recyclerviews

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.Snackbar
import com.itachi1706.busarrivalsg.BusLocationMapsDialogFragment
import com.itachi1706.busarrivalsg.BusServicesAtStopRecyclerActivity
import com.itachi1706.busarrivalsg.R
import com.itachi1706.busarrivalsg.database.BusStopsDb
import com.itachi1706.busarrivalsg.databinding.RecyclerviewBusNumbersBinding
import com.itachi1706.busarrivalsg.objects.BusRecordView
import com.itachi1706.busarrivalsg.objects.BusServices
import com.itachi1706.busarrivalsg.services.BusStorage
import com.itachi1706.busarrivalsg.util.BusesUtil
import com.itachi1706.busarrivalsg.util.StaticVariables
import com.itachi1706.helperlib.helpers.LogHelper.d
import com.itachi1706.helperlib.helpers.PrefHelper
import java.util.Collections

class FavouritesRecyclerAdapter(
    private var items: MutableList<BusServices>,
    private val activity: AppCompatActivity,
    private val serverTime: Boolean
) : RecyclerView.Adapter<FavouritesRecyclerAdapter.FavouritesViewHolder>() {

    private val busStorage: BusStorage
    private var currentTime: String = ""
    private val sp: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)

    init {
        busStorage = BusStorage(sp)
    }

    fun updateAdapter(newObjects: List<BusServices>, currentTime: String?) {
        items = newObjects.toMutableList()
        this.currentTime = currentTime ?: ""
        notifyItemRangeChanged(0, items.size)
    }

    fun moveItem(fromPos: Int, toPos: Int): Boolean {
        if (fromPos < toPos) {
            for (i in fromPos until toPos) {
                Collections.swap(items, i, i + 1)
            }
        } else {
            for (i in fromPos downTo toPos + 1) {
                Collections.swap(items, i, i - 1)
            }
        }

        notifyItemMoved(fromPos, toPos)
        busStorage.updateBusJson(items)
        return true
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FavouritesViewHolder {
        val binding = RecyclerviewBusNumbersBinding.inflate(activity.layoutInflater, parent, false)

        return FavouritesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavouritesViewHolder, position: Int) {
        val i = items[position]

        holder.binding.tvBusOperator.text = i.operator
        holder.binding.tvBusOperator.setTextColor(BusesUtil.getOperatorColor(activity, i.operator))

        holder.binding.tvBusService.text = i.serviceNo
        holder.binding.tvBusStopName.visibility = View.VISIBLE
        holder.binding.tvBusStopName.text = "${i.stopName.trim()} (${i.stopID.trim()})"

        val busNow = BusRecordView(
            holder.binding.tvBusService,
            holder.binding.tvBusOperator,
            holder.binding.btnBusArrivalNow,
            holder.binding.tvBusStatus,
            holder.binding.tvBusStopName,
            holder.binding.ivWheelchairNow,
            holder.binding.tvBusTypeNow,
            serverTime
        )

        val busNext = BusRecordView(
            holder.binding.tvBusService,
            holder.binding.tvBusOperator,
            holder.binding.btnBusArrivalNext,
            holder.binding.tvBusStatus,
            holder.binding.tvBusStopName,
            holder.binding.ivWheelchairNext,
            holder.binding.tvBusTypeNext,
            serverTime
        )

        val busSub = BusRecordView(
            holder.binding.tvBusService,
            holder.binding.tvBusOperator,
            holder.binding.btnBusArrivalSub,
            holder.binding.tvBusStatus,
            holder.binding.tvBusStopName,
            holder.binding.ivWheelchairSub,
            holder.binding.tvBusTypeSub,
            serverTime
        )

        if (!i.isObtainedNextData) {
            busNow.processing()
            busNext.processing()
            busSub.processing()
            return
        }

        if (!i.isSvcStatus) {
            holder.binding.tvBusStatus.text = activity.getString(R.string.service_not_operational)
            holder.binding.tvBusStatus.setTextColor(Color.RED)
            busNow.notArriving()
            busNext.notArriving()
            busSub.notArriving()
            return
        }

        holder.binding.tvBusStatus.text = activity.getString(R.string.service_operational)
        holder.binding.tvBusStatus.setTextColor(
            if (PrefHelper.isNightModeEnabled(activity)) Color.GREEN else ContextCompat.getColor(
                activity,
                R.color.dark_green
            )
        )

        // Current Bus processing
        busNow.setInformationFavourites(
            i.currentBus,
            currentTime,
            ArrivalButton(i, i.stopID, i.serviceNo, StaticVariables.CUR)
        )

        // 2nd bus
        busNext.setInformationFavourites(
            i.nextBus,
            currentTime,
            ArrivalButton(i, i.stopID, i.serviceNo, StaticVariables.NEXT)
        )

        // 3rd bus
        busSub.setInformationFavourites(
            i.subsequentBus,
            currentTime,
            ArrivalButton(i, i.stopID, i.serviceNo, StaticVariables.SUB)
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun removeFavourite(position: Int): Boolean {
        val item = items[position]
        val message = activity.getString(
            R.string.dialog_message_remove_from_fav_with_stop_name,
            item.serviceNo,
            item.stopName,
            item.stopID
        )

        // Add companion devices
        when (sp.getString("companionDevice", "none")) {
            "androidWear" -> {
                // Coming soon
            }

            else -> {
                // Do nothing
            }
        }

        val alert = AlertDialog.Builder(activity).setTitle(R.string.dialog_title_remove_from_fav)
            .setMessage(message)
            .setPositiveButton(android.R.string.yes) { _, _ ->
                // Remove from favourites
                for (i in items.indices) {
                    val s = items[i]
                    if (s.stopID.equals(
                            item.stopID,
                            ignoreCase = true
                        ) && s.serviceNo.equals(item.serviceNo, ignoreCase = true)
                    ) {
                        items.remove(s)
                        break
                    }
                }
                busStorage.updateBusJson(items)
                notifyItemRemoved(position)
                Toast.makeText(
                    activity.applicationContext,
                    R.string.toast_message_remove_from_fav,
                    Toast.LENGTH_SHORT
                ).show()
            }.setNegativeButton(android.R.string.no, null).create()
        alert.setOnDismissListener { notifyItemChanged(position) }
        alert.show()
        return true
    }


    inner class FavouritesViewHolder(val binding: RecyclerviewBusNumbersBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener, View.OnLongClickListener {

        init {
            binding.root.setOnLongClickListener(this)
            binding.root.setOnClickListener(this)
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

        override fun onClick(v: View) {
            val position = layoutPosition
            val item = items[position]
            d("Size", "${items.size}")
            val serviceIntent =
                Intent(activity, BusServicesAtStopRecyclerActivity::class.java).apply {
                    putExtra("stopCode", item.stopID)
                    putExtra("stopName", item.stopName)
                }
            activity.startActivity(serviceIntent)
        }

        override fun onLongClick(v: View): Boolean {
            val position = layoutPosition
            val item = items[position]

            if (sp.getBoolean("showFavHint", true)) {
                val message = activity.getString(
                    R.string.snackbar_message_remove_from_fav_with_stop_name,
                    item.serviceNo
                )
                Snackbar.make(v, message, Snackbar.LENGTH_SHORT).setAction("Hide Tips") {
                    sp.edit { putBoolean("showFavHint", false) }
                }.show()
            }

            return false
        }
    }

    inner class ArrivalButton(
        busObj: BusServices,
        busStopCode: String,
        svcNo: String,
        state: Int
    ) : View.OnClickListener {

        private val longitude: Double
        private val latitude: Double
        private val stopCode: String?
        private val serviceNo: String?
        private val busObj: BusServices
        private val state: Int

        init {
            val status =
                if (state == StaticVariables.CUR) busObj.currentBus else if (state == StaticVariables.NEXT) busObj.nextBus else busObj.subsequentBus
            this.state = state
            this.busObj = busObj
            this.longitude = status!!.longitude
            this.latitude = status.latitude
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
            mapsArgs.putDouble("lat1", busObj.currentBus!!.latitude)
            mapsArgs.putDouble("lng1", busObj.currentBus!!.longitude)
            mapsArgs.putString("arr1", busObj.currentBus!!.estimatedArrival)
            mapsArgs.putDouble("lat2", busObj.nextBus!!.latitude)
            mapsArgs.putDouble("lng2", busObj.nextBus!!.longitude)
            mapsArgs.putString("arr2", busObj.nextBus!!.estimatedArrival)
            mapsArgs.putDouble("lat3", busObj.subsequentBus!!.latitude)
            mapsArgs.putDouble("lng3", busObj.subsequentBus!!.longitude)
            mapsArgs.putString("arr3", busObj.subsequentBus!!.estimatedArrival)
            mapsArgs.putInt("type1", busObj.currentBus!!.busType)
            mapsArgs.putInt("type2", busObj.nextBus!!.busType)
            mapsArgs.putInt("type3", busObj.subsequentBus!!.busType)
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
            dialog.show(activity.getSupportFragmentManager(), "123")
        }
    }
}
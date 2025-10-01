package com.itachi1706.busarrivalsg.adapters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.itachi1706.busarrivalsg.BusServicesAtStopRecyclerActivity
import com.itachi1706.busarrivalsg.databinding.RecyclerviewBusStopsBinding
import com.itachi1706.busarrivalsg.objects.json.ltasg.BusStopJSON
import com.itachi1706.busarrivalsg.util.ShortcutHelper
import com.itachi1706.helperlib.helpers.LogHelper.d
import java.util.Locale

/**
 * This recycler adapter is used in the internal retrieval all bus services from bus stop activity
 */
class BusStopRecyclerAdapter(private var items: MutableList<BusStopJSON>) :
    RecyclerView.Adapter<BusStopRecyclerAdapter.BusStopViewHolder>() {

    fun updateAdapter(newObjects: List<BusStopJSON>?) {
        val oldSize = items.size
        items = newObjects?.toMutableList() ?: mutableListOf()
        if (oldSize == items.size) {
            notifyItemRangeChanged(0, items.size)
        } else {
            notifyItemRangeRemoved(0, oldSize)
            notifyItemRangeInserted(0, items.size)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusStopViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RecyclerviewBusStopsBinding.inflate(inflater, parent, false)
        return BusStopViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BusStopViewHolder, position: Int) {
        val i = items[position]

        holder.binding.tvName.text = i.description
        holder.binding.tvSubText.text =
            if (!i.isHasDistance) "${i.roadName} (${i.busStopCode})" else String.format(
                Locale.getDefault(), "%s (%s) [%.2fm]", i.roadName, i.busStopCode, i.distance
            )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun handleClick(context: Context?, stop: BusStopJSON) {
        d("Size", "${items.size}")
        val serviceIntent = Intent(context, BusServicesAtStopRecyclerActivity::class.java).apply {
            putExtra("stopCode", stop.busStopCode)
            putExtra("stopName", stop.description)
            putExtra("busServices", stop.services)
        }
        context?.startActivity(serviceIntent)

        if (context == null) {
            return // No more operation
        }

        val mFirebaseAnalytics = FirebaseAnalytics.getInstance(context)
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, "Code: ${stop.busStopCode}")
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, "openBusStopDetail")
        }
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)

        // Add dynamic shortcuts
        val shortcutHelper = ShortcutHelper(context)
        shortcutHelper.updateBusStopShortcuts(stop, serviceIntent)
    }

    inner class BusStopViewHolder(val binding: RecyclerviewBusStopsBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            binding.root.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val position = layoutPosition
            val item = items[position]
            handleClick(v.context, item)
        }
    }
}
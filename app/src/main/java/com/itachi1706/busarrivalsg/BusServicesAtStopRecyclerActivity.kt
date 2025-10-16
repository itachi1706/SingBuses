package com.itachi1706.busarrivalsg

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.ArrayMap
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.itachi1706.busarrivalsg.adapters.BusServiceRecyclerAdapter
import com.itachi1706.busarrivalsg.database.BusStopsDb
import com.itachi1706.busarrivalsg.databinding.ActivityBusServicesAtStopRecyclerBinding
import com.itachi1706.busarrivalsg.iface.IFavouritesHandler
import com.itachi1706.busarrivalsg.objects.BusServices
import com.itachi1706.busarrivalsg.objects.json.ltasg.BusArrivalArrayObject
import com.itachi1706.busarrivalsg.objects.json.ltasg.BusArrivalArrayObjectEstimate
import com.itachi1706.busarrivalsg.objects.json.ltasg.BusArrivalMain
import com.itachi1706.busarrivalsg.services.BusStorage
import com.itachi1706.busarrivalsg.tasks.GetBusServicesTask
import com.itachi1706.busarrivalsg.util.StaticVariables
import com.itachi1706.busarrivalsg.util.SwipeFavouriteCallback
import com.itachi1706.helperlib.helpers.EdgeToEdgeHelper
import com.itachi1706.helperlib.helpers.LogHelper
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.lang.ref.WeakReference

class BusServicesAtStopRecyclerActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener, IFavouritesHandler {

    private var busStopCode: String? = null
    private var busStopName: String? = null
    private var busServicesString: String? = null
    private var binding: ActivityBusServicesAtStopRecyclerBinding? = null
    private var adapter: BusServiceRecyclerAdapter? = null

    private val busServices = ArrayMap<String, String>()
    private var busStorage: BusStorage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBusServicesAtStopRecyclerBinding.inflate(layoutInflater)
        EdgeToEdgeHelper.setEdgeToEdgeWithContentView(binding?.root!!, this)
        setSupportActionBar(binding?.toolbar)

        if (intent.hasExtra("stopCode")) busStopCode = intent.getStringExtra("stopCode")
        if (intent.hasExtra("stopName")) busStopName = intent.getStringExtra("stopName")
        if (intent.hasExtra("busServices")) busServicesString = intent.getStringExtra("busServices")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding?.rvBusService?.setHasFixedSize(true)
        val llm = LinearLayoutManager(this)
        llm.orientation = RecyclerView.VERTICAL
        binding?.rvBusService?.layoutManager = llm
        binding?.rvBusService?.itemAnimator = DefaultItemAnimator()

        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        adapter = BusServiceRecyclerAdapter(ArrayList(), this, StaticVariables.useServerTime(sp))
        binding?.rvBusService?.adapter = adapter

        val moveAdapter = ItemTouchHelper(SwipeFavouriteCallback(this, object : SwipeFavouriteCallback.ISwipeCallback {
            override fun getFavouriteState(position: Int): Boolean {
                return checkFavouriteStatus(adapter?.getItem(position))
            }
            override fun moveFavourite(oldPosition: Int, newPosition: Int): Boolean {
                return false
            } // No move

            override fun toggleFavourite(position: Int): Boolean {
                val item = adapter?.getItem(position)
                if (item == null) return false
                // Check and verify based on item
                val fav = BusServices()
                fav.isObtainedNextData = false
                fav.operator = item.operator
                fav.serviceNo = item.serviceNo
                fav.stopID = item.stopCode

                adapter?.notifyItemChanged(position) // Reset item
                favouriteOrUnfavourite(fav, item)
                return false
            }
        }))
        moveAdapter.attachToRecyclerView(binding?.rvBusService)

        binding?.refreshSwipe?.let {
            it.setOnRefreshListener(this)
            it.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3,
                R.color.refresh_progress_4
            )
        }

        if (sp.getBoolean("showHint", true)) {
            Toast.makeText(this, R.string.hint_add_bus_to_fav, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (busStopCode == null) {
            LogHelper.e(TAG, "You aren't supposed to be here. Exiting")
            Toast.makeText(this, R.string.invalid_activity_access, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        supportActionBar?.title = if (busStopName != null) "${busStopName?.trim()} (${busStopCode?.trim()})" else "${busStopCode?.trim()}"
        binding?.refreshSwipe?.isRefreshing = true

        if (busServicesString.isNullOrEmpty()) {
            // Get from DB
            val db = BusStopsDb(this)
            busServicesString = db.getBusStopByBusStopCode(busStopCode)?.services
        }

        val bsWithO = busServicesString?.split(",") ?: listOf()
        busServices.clear()
        for (s in bsWithO) {
            val bs = s.split(":")
            busServices.put(bs[0], bs[1])
        }
        updateBusStop()
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        busStorage = BusStorage(sp)
    }

    private fun addOrRemoveFav(fav: BusServices, all: ArrayList<BusServices>, alrFav: Boolean) {
        if (alrFav) {
            val message = if (busStopName != null) getString(R.string.dialog_message_remove_from_fav_with_stop_name,
                fav.serviceNo, busStopName, fav.stopID) else getString(R.string.dialog_message_remove_from_fav,
                fav.serviceNo, fav.stopID)
            MaterialAlertDialogBuilder(this).setTitle(R.string.dialog_title_remove_from_fav)
                .setMessage(message)
                .setPositiveButton(R.string.yes) { _, _ ->
                    // Remove from favourites
                    for (i in 0 until all.size) {
                        val s = all[i]
                        if (s.stopID.equals(fav.stopID, ignoreCase = true) && s.serviceNo.equals(fav.serviceNo, ignoreCase = true)) {
                            all.removeAt(i)
                            break
                        }
                    }
                    busStorage?.updateBusJson(all)
                    Toast.makeText(this, R.string.toast_message_remove_from_fav, Toast.LENGTH_SHORT).show()
                }.setNegativeButton(R.string.no, null).show()
        } else {
            val message = if (busStopName != null) getString(R.string.dialog_message_add_to_fav_with_stop_name,
                fav.serviceNo, busStopName, fav.stopID) else getString(R.string.dialog_message_add_to_fav,
                fav.serviceNo, fav.stopID)
            MaterialAlertDialogBuilder(this).setTitle(R.string.dialog_title_add_to_fav)
                .setMessage(message)
                .setPositiveButton(R.string.yes) { _, _ ->
                    busStorage?.addNewBus(fav)
                    Toast.makeText(this, R.string.toast_message_add_to_fav, Toast.LENGTH_SHORT).show()
                }.setNegativeButton(R.string.no, null).show()
        }
    }

    private fun updateBusStop() {
        binding?.refreshSwipe?.let {
            it.isRefreshing = true
            GetBusServicesTask(it, this, BusServicesAtStopHandler(this)).execute(busStopCode)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bus_services_at_stop, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, MainSettings::class.java))
                true
            }
            R.id.action_refresh -> {
                binding?.refreshSwipe?.isRefreshing = true
                updateBusStop()
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRefresh() {
        updateBusStop()
    }

    override fun favouriteOrUnfavourite(fav: BusServices, item: BusArrivalArrayObject) {
        busStopName?.let {
            fav.stopName = it
        }

        val exist1 = busStorage?.getStoredBuses() ?: arrayListOf()
        val exist = ArrayList<BusServices>(exist1)
        val alrFavourited = checkFavouriteStatus(exist, item)

        addOrRemoveFav(fav, exist, alrFavourited)
    }

    private fun checkFavouriteStatus(item: BusArrivalArrayObject?): Boolean {
        return checkFavouriteStatus(busStorage?.getStoredBuses(), item)
    }

    private fun checkFavouriteStatus(exist: List<BusServices>?, item: BusArrivalArrayObject?): Boolean {
        exist?.let {
            for (s in it) {
                if (s.serviceNo == item?.serviceNo && s.stopID == item.stopCode) {
                    return true
                }
            }
        }
        return false
    }

    private class BusServicesAtStopHandler(activity: BusServicesAtStopRecyclerActivity) : Handler(
        Looper.getMainLooper()
    ) {
        private val mActivity = WeakReference(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            super.handleMessage(msg)

            LogHelper.d(TAG, "Received message: ${msg.what}")
            if (msg.what == StaticVariables.BUS_SERVICE_JSON_RETRIEVED) {
                val json = msg.data.getString("jsonString")
                activity?.processMessage(json)
            }
        }
    }

    private fun processMessage(json: String?) {
        if (!StaticVariables.checkIfYouGotJsonString(json)) {
            // Retry due to invalid string
            Toast.makeText(this, R.string.toast_message_invalid_json_string, Toast.LENGTH_SHORT).show()
            return
        }
        LogHelper.d(TAG, "Received JSON: $json")
        val jsonConfig = Json { ignoreUnknownKeys = true }

        val items = mutableListOf<BusArrivalArrayObject>()
        if (json == null) return
        val mainArr = try {
            jsonConfig.decodeFromString<BusArrivalMain>(json)
        } catch (e: SerializationException) {
            LogHelper.e(TAG, "Failed to parse JSON: Error: ${e.message}", e)
            Toast.makeText(this, R.string.toast_message_invalid_json_string, Toast.LENGTH_SHORT).show()
            return
        }
        if (mainArr.services == null || mainArr.busStopCode == null) return
        val array = mainArr.services
        val stopId = mainArr.busStopCode
        binding?.refreshSwipe?.isRefreshing = false
        for (obj in array) {
            obj.stopCode = stopId
            // Check service status
            obj.isSvcStatus = true
            items.add(obj)
        }

        // Find all non operational services
        val nonOperational = ArrayMap<String, String>()
        for (svc in busServices.entries) {
            var found = false
            for (i in items) {
                if (svc.key.trim() == i.serviceNo.trim()) {
                    found = true
                    break
                }
            }
            if (!found) nonOperational[svc.key] = svc.value
        }

        // Add all non operational services to the end of the list
        for (s in nonOperational.entries) {
            val notOpObj = BusArrivalArrayObject(
                serviceNo = s.key,
                operator = s.value,
                nextBus = BusArrivalArrayObjectEstimate(estimatedArrival = "", latitude = "", longitude = ""),
                nextBus2 = BusArrivalArrayObjectEstimate(estimatedArrival = "", latitude = "", longitude = ""),
                nextBus3 = BusArrivalArrayObjectEstimate(estimatedArrival = "", latitude = "", longitude = ""),
                isSvcStatus = false,
                stopCode = stopId
            )
            items.add(notOpObj)
        }

        adapter?.updateAdapter(items, mainArr.currentTime)
    }

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }

        private const val TAG = "BusServicesAtStop"
    }
}
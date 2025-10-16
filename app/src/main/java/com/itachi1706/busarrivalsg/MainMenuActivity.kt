package com.itachi1706.busarrivalsg

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationChannelGroupCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.itachi1706.appupdater.AppUpdateInitializer
import com.itachi1706.appupdater.`object`.CAAnalytics
import com.itachi1706.appupdater.utils.AnalyticsHelper
import com.itachi1706.busarrivalsg.adapters.FavouritesRecyclerAdapter
import com.itachi1706.busarrivalsg.database.BusStopsDb
import com.itachi1706.busarrivalsg.databinding.ActivityMainMenuRecyclerBinding
import com.itachi1706.busarrivalsg.services.BusStorage
import com.itachi1706.busarrivalsg.tasks.GetBusServicesFavouritesTask
import com.itachi1706.busarrivalsg.tasks.UpdateDatabaseTask
import com.itachi1706.busarrivalsg.util.LogInitializer
import com.itachi1706.busarrivalsg.util.StaticVariables
import com.itachi1706.busarrivalsg.util.SwipeFavouriteCallback
import com.itachi1706.busarrivalsg.util.SwipeMoveFavouriteCallback
import com.itachi1706.helperlib.helpers.ConnectivityHelper
import com.itachi1706.helperlib.helpers.EdgeToEdgeHelper
import com.itachi1706.helperlib.helpers.LogHelper
import com.itachi1706.helperlib.helpers.PrefHelper
import kotlinx.coroutines.async
import java.util.concurrent.TimeUnit

class MainMenuActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var sp: SharedPreferences
    private var busStorage: BusStorage? = null

    private lateinit var binding: ActivityMainMenuRecyclerBinding
    private var adapter: FavouritesRecyclerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG
        LogInitializer.initLogger()

        binding = ActivityMainMenuRecyclerBinding.inflate(layoutInflater)
        EdgeToEdgeHelper.setEdgeToEdgeWithContentView(binding.root, this)
        setSupportActionBar(binding.toolbar)

        // Obtain Firebase Analytics instance
        val analyticsTask = lifecycleScope.async { loadAnalytics() }
        analyticsTask.invokeOnCompletion { _ -> LogHelper.d(TAG, "Firebase Analytics initialization complete") }

        val llm = LinearLayoutManager(this)
        llm.orientation = RecyclerView.VERTICAL

        binding.refreshFavourites.let {
            it.setOnRefreshListener(this)
            it.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3,
                R.color.refresh_progress_4
            )
        }

        sp = PreferenceManager.getDefaultSharedPreferences(this)
        PrefHelper.handleDefaultThemeSwitch(sp.getString("app_theme", "default") ?: "default")
        sp.edit { putBoolean("cepas_dark_theme", true) }
        adapter = FavouritesRecyclerAdapter(arrayListOf(), this, StaticVariables.useServerTime(sp))
        binding.rvFav.let {
            it.setHasFixedSize(true)
            it.layoutManager = llm
            it.itemAnimator = DefaultItemAnimator()
            it.adapter = adapter
        }

        busStorage = BusStorage(sp)

        val moveAdapter = ItemTouchHelper(SwipeMoveFavouriteCallback(this, object: SwipeFavouriteCallback.ISwipeCallback {
            override fun getFavouriteState(position: Int): Boolean {
                return true // Always favourite here
            }

            override fun moveFavourite(oldPosition: Int, newPosition: Int): Boolean {
                return adapter?.moveItem(oldPosition, newPosition) ?: false
            }

            override fun toggleFavourite(position: Int): Boolean {
                return adapter?.removeFavourite(position) ?: false
            }
        }))
        moveAdapter.attachToRecyclerView(binding.rvFav)

        if (savedInstanceState == null) {
            LogHelper.d(TAG, "Checking for app updates...")
            AppUpdateInitializer(this, sp, R.drawable.notification_icon, StaticVariables.BASE_SERVER_URL, true)
                .setOnlyOnWifiCheck(true).setPathBasedApi(true).checkForUpdate()
            LogHelper.d(TAG, "onCreate complete")
        } else {
            LogHelper.d(TAG, "Skipping app update check as it should be already done")
        }


        // Create Firebase Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createFirebaseNotifChannel()
        }
    }

    @WorkerThread
    fun loadAnalytics() {
        val mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val helper = AnalyticsHelper(this, true)
        val analytics = helper.getData(BuildConfig.DEBUG)
        setAnalyticsData(analytics != null, mFirebaseAnalytics, analytics)
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null)
        LogHelper.d(TAG, "Firebase Analytics data set")
    }

    override fun onResume() {
        super.onResume()

        binding.addFab.let {
            it.setOnClickListener { startActivity(Intent(this, BusStopsTabbedActivity::class.java)) }
            it.setOnLongClickListener { _ ->
                Toast.makeText(this, R.string.fab_hint_main_menu, Toast.LENGTH_SHORT).show()
                return@setOnLongClickListener true
            }
        }

        checkIfDatabaseUpdated()

        binding.refreshFavourites.isRefreshing = true
        updateFavourites()

        // Companion objects
        when (sp.getString("companionDevice", "none")) {
            "pebble" -> LogHelper.i(TAG, "Pebble selected but deprecated. No action taken")
            else -> LogHelper.i(TAG, "No companion device selected")
        }

        binding.firebaseSyncStatus.let {
            it.isClickable = true
            it.setOnClickListener { startActivity(Intent(this, FirebaseLoginActivity::class.java)) }
        }

        invalidateOptionsMenu()
    }

    private fun setAnalyticsData(enabled: Boolean, firebaseAnalytics: FirebaseAnalytics, analytics: CAAnalytics?) {
        firebaseAnalytics.setUserProperty("debug_mode", if (enabled) analytics?.isDebug.toString() else null)
        firebaseAnalytics.setUserProperty("device_manufacturer", if (enabled) analytics?.getdManufacturer() else null)
        firebaseAnalytics.setUserProperty("device_codename", if (enabled) analytics?.getdCodename() else null)
        firebaseAnalytics.setUserProperty("device_fingerprint", if (enabled) analytics?.getdFingerprint() else null)
        firebaseAnalytics.setUserProperty("device_cpu_abi", if (enabled) analytics?.getdCPU() else null)
        firebaseAnalytics.setUserProperty("device_tags", if (enabled) analytics?.getdTags() else null)
        firebaseAnalytics.setUserProperty("app_version_code", if (enabled) analytics?.appVerCode.toString() else null)
        firebaseAnalytics.setUserProperty("android_sec_patch", if (enabled) analytics?.sdkPatch else null)
        firebaseAnalytics.setUserProperty("AndroidOS", if (enabled) analytics?.sdkver.toString() else null)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.ntu_tracker).isVisible = sp.getBoolean("showntushuttle", false)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, MainSettings::class.java))
                true
            }
            R.id.view_all_stops -> {
                startActivity(Intent(this, ListAllBusStopsActivity::class.java))
                true
            }
            R.id.action_refresh -> {
                binding.refreshFavourites.isRefreshing = true
                updateFavourites()
                true
            }
            R.id.ntu_tracker -> {
                startActivity(Intent(this, NtuBusActivity::class.java))
                true
            }
            R.id.scan_cepas -> {
                startActivity(Intent(this, CEPASScanActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateFavourites() {
        val innerTag = "FAVOURITES:"
        // Populate favourites
        LogHelper.d(TAG, "$innerTag Favourited Pref: ${sp.getString("stored", "wot")}")

        if (busStorage?.hasFavourites() ?: false) {
            // Load and get data
            LogHelper.d(TAG, "$innerTag Has favourites, processing...")
            StaticVariables.favouritesList = ArrayList(busStorage?.getStoredBuses() ?: emptyList())
            adapter?.let {
                it.updateAdapter(StaticVariables.favouritesList, null)
                it.notifyItemRangeChanged(0, StaticVariables.favouritesList.size)

                LogHelper.d(TAG, "$innerTag Finished Processing, retrieving estimated arrival data...")
                GetBusServicesFavouritesTask(this, it).executeOnExecutor(*StaticVariables.favouritesList.toTypedArray())
                LogHelper.d(TAG, "$innerTag AsyncTask created to retrieved estimated arrival data")
            }
        }

        binding.refreshFavourites.let {
            if (it.isRefreshing) it.isRefreshing = false
        }
    }

    private fun checkIfDatabaseUpdated() {
        val busDbLastUpdate = sp.getLong(BUS_DB_TIME_UPDATE_CHECK, -1)
        var busDbUpdate = false
        if (busDbLastUpdate != -1L) {
            val day = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - busDbLastUpdate)
            LogHelper.d(TAG, "Init: Days since Bus DB last update: $day")
            if (day > 30) {
                busDbUpdate = true
            }
        }

        // Check for upgrades
        val dbTag = "DB UPGRADE"
        val dbVer = sp.getInt(DB_VERSION_CHECK, 0)
        LogHelper.d(TAG, "$dbTag: Current DB Version: $dbVer")
        when (dbVer) {
            0, 1 -> {
                // Upgrade to 2
                LogHelper.i(TAG, "$dbTag: Upgrading to V2 API DB")
                busDbUpdate = true
                sp.edit { putInt(DB_VERSION_CHECK, 2) }
            }
            2 -> {
                // Upgrade to 3
                LogHelper.i(TAG, "$dbTag: Upgrading to DB with Bus Services")
                busDbUpdate = true
                sp.edit { putInt(DB_VERSION_CHECK, 3) }
            }
            else -> LogHelper.d(TAG, "$dbTag: No DB upgrade required")
        }

        // Main DB
        if (!sp.getBoolean("busDBLoaded", false) || busDbUpdate) {
            // First boot, populate DB
            if (!ConnectivityHelper.hasInternetConnection(applicationContext)) {
                networkUnavailable(getString(R.string.database_name_bus))
            } else {
                LogHelper.d(TAG, "Init: Initializing Bus Stop Database")
                if (busDbUpdate) {
                    BusStopsDb(this).use {
                        it.dropAndRebuildDb() // Rebuild DB
                        LogHelper.i(TAG, "$dbTag: Database upgraded")
                    }
                }

                ContextCompat.startForegroundService(this, Intent(this, UpdateDatabaseTask::class.java))
            }
        } else {
            // Legacy Check
            if (sp.getLong(BUS_DB_TIME_UPDATE_CHECK, -1) == -1L) {
                sp.edit { putLong(BUS_DB_TIME_UPDATE_CHECK, System.currentTimeMillis()) }
            }
        }
    }

    private fun networkUnavailable(reason: String) {
        MaterialAlertDialogBuilder(this).setTitle(R.string.dialog_title_no_internet)
            .setMessage(getString(R.string.dialog_message_no_internet, reason))
            .setCancelable(false)
            .setNeutralButton(R.string.dialog_action_neutral_override, null)
            .setPositiveButton(android.R.string.ok) { _, _ -> finish() }
            .show()
    }

    override fun onRefresh() {
        updateFavourites()
    }

    private fun createFirebaseNotifChannel() {
        val notificationManager = NotificationManagerCompat.from(this)
        val notificationChannel = NotificationChannelCompat.Builder("firebase-msg", NotificationManagerCompat.IMPORTANCE_LOW)
            .setName("Server Alerts (FB)").setLightsEnabled(true)
            .setLightColor(Color.RED)
            .setVibrationEnabled(true)
            .setVibrationPattern(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
            .setGroup("server-msg")
            .build()

        val notificationChannelGroup = NotificationChannelGroupCompat.Builder("server-msg")
            .setName("Server Messages")
            .build()

        notificationManager.createNotificationChannelGroup(notificationChannelGroup)
        notificationManager.createNotificationChannel(notificationChannel)
    }

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }

        private const val DB_VERSION_CHECK = "busDBVerCheck"
        private const val BUS_DB_TIME_UPDATE_CHECK = "busDBTimeUpdated"

        private const val TAG = "MainMenuActivity"
    }
}
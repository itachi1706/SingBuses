package com.itachi1706.busarrivalsg

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.itachi1706.appupdater.EasterEggResMultiMusicPrefFragment
import com.itachi1706.appupdater.SettingsInitializer
import com.itachi1706.appupdater.Util.DeprecationHelper
import com.itachi1706.appupdater.Util.PrefHelper
import com.itachi1706.busarrivalsg.Services.LocManager
import com.itachi1706.busarrivalsg.util.StaticVariables
import com.itachi1706.cepaslib.SettingsHandler
import de.psdev.licensesdialog.LicensesDialog
import java.util.*

/**
 * Created by Kenneth on 26/7/2019.
 * for com.itachi1706.busarrivalsg in SingBuses
 */
class MainSettings : AppCompatActivity() {
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        supportFragmentManager.beginTransaction().replace(android.R.id.content, GeneralPreferenceFragment())
                .commit()
    }

    class GeneralPreferenceFragment : EasterEggResMultiMusicPrefFragment() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_general)

            val sp = PreferenceManager.getDefaultSharedPreferences(activity?.applicationContext)

            SettingsHandler(activity!!).initSettings(this)

            SettingsInitializer().setFullscreen(true).explodeUpdaterSettings(activity, R.drawable.notification_icon, StaticVariables.BASE_SERVER_URL,
                    resources.getString(R.string.link_legacy), resources.getString(R.string.link_updates), this)
            super.addEggMethods(true) {
                LicensesDialog.Builder(activity!!).setNotices(R.raw.notices).setIncludeOwnLicense(true).build().show()
                false
            }

            val favJson = findPreference<Preference>("fav_json")
            favJson?.setOnPreferenceClickListener {
                val json = sp.getString("stored", "No Favourites")
                AlertDialog.Builder(activity!!).setMessage(json).setTitle("Favourites JSON String").setPositiveButton("Close", null).show()
                true
            }

            val timeDBUpdateBus = findPreference<Preference>("busDBTimeUpdated")
            val dbBus = sp.getLong("busDBTimeUpdated", -1)
            updateSummaryDBBus(timeDBUpdateBus, dbBus)

            findPreference<Preference>("companionDevice")?.setOnPreferenceChangeListener { _, o ->
                val companion = o as String
                Log.d("DEBUG", "Companion: $companion")
                fragmentManager!!.beginTransaction().replace(android.R.id.content, GeneralPreferenceFragment()).commit()
                true
            }

            val nearbyStopsCount = findPreference<Preference>("nearbyStopsCount")
            nearbyStopsCount?.summary = (nearbyStopsCount as EditTextPreference).text
            nearbyStopsCount.setOnPreferenceChangeListener { preference, newValue ->
                preference.summary = newValue.toString()
                true
            }

            val shuttleRefreshRate = findPreference<Preference>("ntushuttlerefrate") as EditTextPreference
            shuttleRefreshRate.summary = resources.getQuantityString(R.plurals.seconds_count, Integer.parseInt(shuttleRefreshRate.text), Integer.parseInt(shuttleRefreshRate.text))
            shuttleRefreshRate.dialogTitle = "NTU Shuttle Tracker Auto-Refresh"
            shuttleRefreshRate.dialogMessage = "Tweaks the auto refresh rate (in seconds) of the NTU Shuttle Bus Tracking\nMinimum time is 5 seconds"
            shuttleRefreshRate.setOnPreferenceChangeListener { preference, newValue ->
                preference.summary = resources.getQuantityString(R.plurals.seconds_count, Integer.parseInt(newValue.toString()), Integer.parseInt(newValue.toString()))
                true
            }

            findPreference<Preference>("location_value")?.setOnPreferenceClickListener { processAdvDevSettingLocation() }

            findPreference<Preference>("app_theme")?.setOnPreferenceChangeListener { _, newValue -> PrefHelper.handleDefaultThemeSwitch(newValue.toString()); true }
        }

        private fun updateSummaryDBBus(timeDBUpdateBus: Preference?, dbBus: Long) {
            if (dbBus == (-1).toLong()) {
                timeDBUpdateBus?.summary = "Never"
                return
            }
            val date = Date(dbBus)
            timeDBUpdateBus?.summary = StaticVariables.convertDateToString(date)
        }

        override fun getMusicResource(): Int {
            return if (randomNumberIsEven()) R.raw.juss else R.raw.bfltw
        }

        override fun getStartEggMessage(): String {
            return if (randomNumberIsEven()) "~It's time to jump up in the air!~" else "~Just lead the way (and I'll follow you)~"
        }

        override fun getEndEggMessage(): String {
            return if (randomNumberIsEven()) "~I'll be your 1-UP girl~" else "~And we'll grab the flag together the fireworks are gonna start~"
        }

        override fun getStopEggButtonText(): String {
            return if (randomNumberIsEven()) "1-UP" else "Break Free"
        }

        private fun processAdvDevSettingLocation(): Boolean {
            if (ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder(activity!!).setTitle("Location Permission not granted")
                        .setMessage("Location permission is not granted. Enable from the App Settings page or by scanning for nearby bus stops")
                        .setNeutralButton(R.string.dialog_action_neutral_app_settings) { _, _ ->
                            val permIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val packageURI = Uri.parse("package:" + activity!!.packageName)
                            permIntent.data = packageURI
                            startActivity(permIntent)
                        }.setPositiveButton(R.string.dialog_action_positive_close, null).show()
                return true
            }

            val gpsManager = LocManager(activity)
            gpsManager.location
            val gps = gpsManager.gpsLoc
            val net = gpsManager.netLoc
            val message = StringBuilder()
            if (gps != null) {
                message.append("<b>GPS Data</b><br>")
                message.append(getLocationRawStringData(gps)).append("<br><br><br>")
            }
            if (net != null) {
                message.append("<b>Mobile Network Data</b><br>")
                message.append(getLocationRawStringData(net))
            }
            AlertDialog.Builder(activity!!).setTitle("Location Data")
                    .setMessage(DeprecationHelper.Html.fromHtml(message.toString()))
                    .setPositiveButton(R.string.dialog_action_positive_close, null).show()
            return true
        }

        private fun getLocationRawStringData(loc: Location): String {
            return String.format(Locale.getDefault(), "Latitude: %f <br>Longitude: %f <br>", loc.latitude, loc.longitude) +
                    String.format(Locale.getDefault(), "Altitude: %f <br>Accuracy: %f <br>", loc.altitude, loc.accuracy) +
                    String.format(Locale.getDefault(), "Bearing: %f <br>Speed: %f <br>", loc.bearing, loc.speed) +
                    String.format(Locale.getDefault(), "Time: %d <br>Provider: %s", loc.time, loc.provider)
        }
    }
}
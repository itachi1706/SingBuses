package com.itachi1706.busarrivalsg;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.itachi1706.appupdater.EasterEggResMultiMusicPrefFragment;
import com.itachi1706.appupdater.SettingsInitializer;
import com.itachi1706.appupdater.Util.DeprecationHelper;
import com.itachi1706.busarrivalsg.Services.LocManager;
import com.itachi1706.busarrivalsg.Util.StaticVariables;

import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import de.psdev.licensesdialog.LicensesDialog;


/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class MainSettings extends AppCompatActivity {

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new GeneralPreferenceFragment())
                .commit();
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @SuppressWarnings("ConstantConditions")
    public static class GeneralPreferenceFragment extends EasterEggResMultiMusicPrefFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            handleCompanions(sp);

            new SettingsInitializer(getActivity(), R.drawable.notification_icon, StaticVariables.BASE_SERVER_URL,
                    getResources().getString(R.string.link_legacy), getResources().getString(R.string.link_updates), true)
                    .explodeUpdaterSettings(this);
            super.addEggMethods(true, preference -> {
                new LicensesDialog.Builder(getActivity()).setNotices(R.raw.notices)
                        .setIncludeOwnLicense(true).build().show();
                return false;
            });

            Preference favJson = findPreference("fav_json");
            favJson.setOnPreferenceClickListener(preference -> {
                String json = sp.getString("stored", "No Favourites");
                new AlertDialog.Builder(getActivity()).setMessage(json).setTitle("Favourites JSON String")
                        .setPositiveButton("Close", null).show();
                return true;
            });

            Preference timeDBUpdateBus = findPreference("busDBTimeUpdated");
            long dbBus = sp.getLong("busDBTimeUpdated", -1);
            updateSummaryDBBus(timeDBUpdateBus, dbBus);

            findPreference("companionDevice").setOnPreferenceChangeListener((preference, o) -> {
                String companion = (String) o;
                Log.d("DEBUG", "Companion: " + companion);
                getFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new GeneralPreferenceFragment())
                        .commit();
                return true;
            });

            Preference nearbyStopsCount = findPreference("nearbyStopsCount");
            nearbyStopsCount.setSummary(((EditTextPreference)nearbyStopsCount).getText());
            nearbyStopsCount.setOnPreferenceChangeListener((preference, newValue) -> {
                preference.setSummary(String.valueOf(newValue));
                return true;
            });

            findPreference("location_value").setOnPreferenceClickListener(preference -> processAdvDevSettingLocation());
        }

        private void updateSummaryDBBus(Preference timeDBUpdateBus, long dbBus){
            if (dbBus == -1) {
                timeDBUpdateBus.setSummary("Never");
                return;
            }
            Date date = new Date(dbBus);
            timeDBUpdateBus.setSummary(StaticVariables.convertDateToString(date));
        }

        // Handling of all companion devices starts here
        private void handleCompanions(SharedPreferences sharedPreferences) {
            switch (sharedPreferences.getString("companionDevice", "none")) {
                case "pebble": addPreferencesFromResource(R.xml.pref_pebble); break;
                case "none":
                    default: sharedPreferences.edit().putBoolean("pebbleSvc", false).apply(); break;
            }
        }

        @Override
        public int getMusicResource() {
            if (randomNumberIsEven()) return R.raw.juss;
            return R.raw.bfltw;
        }

        @Override
        public String getStartEggMessage() {
            if (randomNumberIsEven()) return "~It's time to jump up in the air!~";
            return "~Just lead the way (and I'll follow you)~";
        }

        @Override
        public String getEndEggMessage() {
            if (randomNumberIsEven()) return "~I'll be your 1-UP girl~";
            return "~And we'll grab the flag together the fireworks are gonna start~";
        }

        @Override
        public String getStopEggButtonText() {
            if (randomNumberIsEven()) return "1-UP";
            return "Break Free";
        }

        private boolean processAdvDevSettingLocation() {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                new AlertDialog.Builder(getActivity()).setTitle("Location Permission not granted")
                        .setMessage("Location permission is not granted. Enable from the App Settings page or by scanning for nearby bus stops")
                        .setNeutralButton(R.string.dialog_action_neutral_app_settings, (dialog, which) -> {
                            Intent permIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri packageURI = Uri.parse("package:" + getActivity().getPackageName());
                            permIntent.setData(packageURI);
                            startActivity(permIntent);
                        }).setPositiveButton(R.string.dialog_action_positive_close, null).show();
                return true;
            }

            LocManager gpsManager = new LocManager(getActivity());
            gpsManager.getLocation();
            Location gps = gpsManager.getGpsLoc();
            Location net = gpsManager.getNetLoc();
            StringBuilder message = new StringBuilder();
            if (gps != null) {
                message.append("<b>GPS Data</b><br>");
                message.append(getLocationRawStringData(gps)).append("<br><br><br>");
            }
            if (net != null) {
                message.append("<b>Mobile Network Data</b><br>");
                message.append(getLocationRawStringData(net));
            }
            new AlertDialog.Builder(getActivity()).setTitle("Location Data")
                    .setMessage(DeprecationHelper.Html.fromHtml(message.toString()))
                    .setPositiveButton(R.string.dialog_action_positive_close, null).show();
            return true;
        }

        private String getLocationRawStringData(Location loc) {
            return String.format(Locale.getDefault(), "Latitude: %f <br>Longitude: %f <br>", loc.getLatitude(), loc.getLongitude()) +
                    String.format(Locale.getDefault(), "Altitude: %f <br>Accuracy: %f <br>", loc.getAltitude(), loc.getAccuracy()) +
                    String.format(Locale.getDefault(), "Bearing: %f <br>Speed: %f <br>", loc.getBearing(), loc.getSpeed()) +
                    String.format(Locale.getDefault(), "Time: %d <br>Provider: %s", loc.getTime(), loc.getProvider());
        }
    }
}

package com.itachi1706.busarrivalsg;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.itachi1706.appupdater.EasterEggResMultiMusicPrefFragment;
import com.itachi1706.appupdater.SettingsInitializer;
import com.itachi1706.busarrivalsg.Util.StaticVariables;

import java.util.Date;

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
        }

        private void updateSummaryDBBus(Preference timeDBUpdateBus, long dbBus){
            if (dbBus == -1) {
                timeDBUpdateBus.setSummary("Never");
                return;
            }
            Date date = new Date(dbBus);
            timeDBUpdateBus.setSummary(date.toString());
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
    }
}

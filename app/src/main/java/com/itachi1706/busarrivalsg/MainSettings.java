package com.itachi1706.busarrivalsg;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.itachi1706.appupdater.SettingsInitializer;
import com.itachi1706.busarrivalsg.Util.StaticVariables;

import java.util.Date;


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
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

            new SettingsInitializer(getActivity(), R.drawable.notification_icon, StaticVariables.BASE_SERVER_URL,
                    getResources().getString(R.string.link_legacy), getResources().getString(R.string.link_updates), true)
                    .explodeInfoSettings(this).explodeUpdaterSettings(this);

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
        }

        private void updateSummaryDBBus(Preference timeDBUpdateBus, long dbBus){
            if (dbBus == -1) {
                timeDBUpdateBus.setSummary("Never");
                return;
            }
            Date date = new Date(dbBus);
            timeDBUpdateBus.setSummary(date.toString());
        }
    }
}

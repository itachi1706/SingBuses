package com.itachi1706.busarrivalsg;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.itachi1706.appupdater.AppUpdateChecker;
import com.itachi1706.appupdater.Util.UpdaterHelper;
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

            //Debug Info Get
            String version = "NULL", packName = "NULL";
            int versionCode = 0;
            try {
                PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                version = pInfo.versionName;
                packName = pInfo.packageName;
                versionCode = pInfo.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            Preference verPref = findPreference("view_app_version");
            verPref.setSummary(version + "-b" + versionCode);
            Preference pNamePref = findPreference("view_app_name");
            pNamePref.setSummary(packName);
            Preference prefs = findPreference("view_sdk_version");
            prefs.setSummary(android.os.Build.VERSION.RELEASE);

            findPreference("launch_updater").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new AppUpdateChecker(getActivity(), sp, R.mipmap.ic_launcher, StaticVariables.BASE_SERVER_URL).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    return false;
                }
            });

            findPreference("android_changelog").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    UpdaterHelper.settingGenerateChangelog(sp, getActivity());
                    return true;
                }
            });

            Preference oldVersionPref = findPreference("get_old_app");
            oldVersionPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(getResources().getString(R.string.link_legacy)));
                    startActivity(i);
                    return false;
                }
            });

            Preference latestVersionPref = findPreference("get_latest_app");
            latestVersionPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(getResources().getString(R.string.link_updates)));
                    startActivity(i);
                    return false;
                }
            });

            Preference favJson = findPreference("fav_json");
            favJson.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    String json = sp.getString("stored", "No Favourites");
                    new AlertDialog.Builder(getActivity()).setMessage(json).setTitle("Favourites JSON String")
                            .setPositiveButton("Close", null).show();
                    return true;
                }
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

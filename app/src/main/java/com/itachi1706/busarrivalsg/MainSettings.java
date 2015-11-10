package com.itachi1706.busarrivalsg;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;

import com.itachi1706.busarrivalsg.AsyncTasks.Updater.AppUpdateCheck;
import com.itachi1706.busarrivalsg.Util.StaticVariables;

import java.util.ArrayList;
import java.util.Collections;
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

            final Preference updaterPref = findPreference("launch_updater");
            updaterPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new AppUpdateCheck(getActivity(), sp).execute();
                    return false;
                }
            });

            Preference changelogPref = findPreference("android_changelog");
            changelogPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    String changelog = sp.getString("version-changelog", "l");
                    if (changelog.equals("l")) {
                        //Not available
                        new android.app.AlertDialog.Builder(getActivity()).setTitle(R.string.dialog_title_no_changelog)
                                .setMessage(R.string.dialog_message_no_changelog)
                                .setPositiveButton(android.R.string.ok, null).show();
                    } else {
                        String[] changelogArr = changelog.split("\n");
                        ArrayList<String> changelogArrList = new ArrayList<>();
                        Collections.addAll(changelogArrList, changelogArr);
                        String body = StaticVariables.getChangelogStringFromArrayList(changelogArrList);
                        new android.app.AlertDialog.Builder(getActivity()).setTitle(R.string.dialog_title_changelog)
                                .setMessage(Html.fromHtml(body)).setPositiveButton(R.string.dialog_action_positive_close, null).show();
                    }
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
            Preference timeDBUpdateGeo = findPreference("geoDBTimeUpdated");
            long dbBus = sp.getLong("busDBTimeUpdated", -1);
            long dbGeo = sp.getLong("busDBTimeUpdated", -1);
            updateSummaryDBBus(timeDBUpdateBus, dbBus);
            updateSummaryDBGeo(timeDBUpdateGeo, dbGeo);

        }

        private void updateSummaryDBBus(Preference timeDBUpdateBus, long dbBus){
            if (dbBus == -1) {
                timeDBUpdateBus.setSummary("Never");
                return;
            }
            Date date = new Date(dbBus);
            timeDBUpdateBus.setSummary(date.toString());
        }

        private void updateSummaryDBGeo(Preference timeDBUpdateGeo, long dbGeo){
            if (dbGeo == -1) {
                timeDBUpdateGeo.setSummary("Never");
                return;
            }
            Date date = new Date(dbGeo);
            timeDBUpdateGeo.setSummary(date.toString());
        }
    }
}

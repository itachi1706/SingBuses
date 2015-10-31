package com.itachi1706.busarrivalsg.AsyncTasks.Updater;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;

import com.itachi1706.busarrivalsg.NotifyUserUtil;
import com.itachi1706.busarrivalsg.R;
import com.itachi1706.busarrivalsg.StaticVariables;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Kenneth on 16/2/2015, 6:59 PM
 * for Hypixel Statistics in package com.itachi1706.hypixelstatistics.AsyncAPI
 */
public class AppUpdateCheck extends AsyncTask<Void, Void, String> {

    Activity mActivity;
    Exception except = null;
    SharedPreferences sp;
    ArrayList<String> changelogStrings = new ArrayList<>();
    boolean main = false;

    public AppUpdateCheck(Activity activity, SharedPreferences sharedPrefs){
        mActivity = activity;
        sp = sharedPrefs;
    }

    public AppUpdateCheck(Activity activity, SharedPreferences sharedPrefs, boolean isMain){
        mActivity = activity;
        sp = sharedPrefs;
        main = isMain;
    }


    @Override
    protected String doInBackground(Void... params) {
        String url = "http://android.itachi1706.com/android/updates/singbuses.html";
        String tmp = "";
        try {
            URL urlConn = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlConn.openConnection();
            conn.setConnectTimeout(StaticVariables.HTTP_QUERY_TIMEOUT);
            conn.setReadTimeout(StaticVariables.HTTP_QUERY_TIMEOUT);
            InputStream in = conn.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder str = new StringBuilder();
            String line;
            changelogStrings.clear();
            while((line = reader.readLine()) != null)
            {
                str.append(line).append("\n");
                changelogStrings.add(line);
            }
            in.close();
            tmp = str.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tmp;
    }

    protected void onPostExecute(String changelog){
        if (except != null){
            NotifyUserUtil.createShortToast(mActivity.getApplicationContext(), "Unable to contact update server to check for updates");
            return;
        }
         /* Legend of Stuff
        1st Line - Current Version Code check
        2nd Line - Current Version Number
        3rd Line - Link to New Version
        # - Changelog Version Number (Bold this)
        * - Points
         */
        if (changelogStrings.size() <= 0){
            NotifyUserUtil.createShortToast(mActivity.getApplicationContext(), "Unable to do app update check");
            return;
        }
        sp.edit().putString("version-changelog", changelog).apply();
        int serverVersionCode = Integer.parseInt(changelogStrings.get(0));
        int localVersionCode = 0;
        final String newVersionURL = changelogStrings.get(2);
        PackageInfo pInfo;
        try {
            pInfo = mActivity.getApplicationContext().getPackageManager().getPackageInfo(mActivity.getApplicationContext().getPackageName(), 0);
            localVersionCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Log.d("VERSION-SERVER", "Version on Server: " + serverVersionCode);
        Log.d("VERSION-LOCAL", "Current Version: " + localVersionCode);
        boolean hasUpdate = compareVersions(localVersionCode, serverVersionCode);
        if (hasUpdate){
            Log.d("UPDATE NEEDED", "An Update is needed");
            //Outdated Version. Prompt Update
            String bodyMsg = StaticVariables.getChangelogStringFromArrayList(changelogStrings);
            String title = "A New Update is Available!";
            if (!mActivity.isFinishing()) {
                new AlertDialog.Builder(mActivity).setTitle(title).setMessage(Html.fromHtml(bodyMsg))
                        .setNegativeButton("Don't Update", null).setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NotificationManager manager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mActivity);
                        mBuilder.setContentTitle("Downloading new update").setContentText("Downloading new update...")
                                .setProgress(0,0,true).setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(false)
                        .setOngoing(true).setTicker("Downloading new update to the app");
                        Random random = new Random();
                        int notificationId = random.nextInt();
                        manager.notify(notificationId, mBuilder.build());
                        new DownloadAndInstallApp(mActivity, mBuilder, manager, notificationId).execute(newVersionURL);
                    }
                }).show();
            }
            return;
        }
        if (!main){
            Log.d("UPDATE CHECK", "No Update Needed");
            if (!mActivity.isFinishing()) {
                new AlertDialog.Builder(mActivity).setTitle("Check for New Update").setMessage("You are on the latest release! No update is required.")
                        .setNegativeButton("Close", null).show();
            } else {
                NotifyUserUtil.createShortToast(mActivity.getApplicationContext(), "No update is required");
            }
        }
    }

    private boolean compareVersions(int local, int server){
        return local < server;
    }
}

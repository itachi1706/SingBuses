package com.itachi1706.busarrivalsg.AsyncTasks.Updater;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.itachi1706.busarrivalsg.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Kenneth on 12/5/2015
 * for HypixelStatistics in package com.itachi1706.hypixelstatistics.AsyncAPI
 */
public class DownloadAndInstallApp extends AsyncTask<String, Float, Boolean> {

    private Activity activity;
    Exception except = null;
    private Uri link;
    private String filePATH;
    private NotificationCompat.Builder notification;
    private NotificationManager manager;
    private int notificationID;
    private boolean ready = false;

    public DownloadAndInstallApp(Activity activity, NotificationCompat.Builder notificationBuilder, NotificationManager notifyManager, int notifcationID){
        this.activity = activity;
        this.notification = notificationBuilder;
        this.manager = notifyManager;
        this.notificationID = notifcationID;
    }

    @Override
    protected Boolean doInBackground(String... updateLink) {
        try {
            link = Uri.parse(updateLink[0]);
            URL url = new URL(updateLink[0]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(60000);
            conn.setReadTimeout(60000);
            conn.setRequestMethod("GET");
            conn.connect();
            Log.d("DL", "Starting Download...");

            filePATH = activity.getApplicationContext().getExternalFilesDir(null) + File.separator + "download" + File.separator;
            File folder = new File(filePATH);
            if (!folder.exists()) {
                if (!tryAndCreateFolder(folder)) {
                    Log.d("Fail", "Cannot Create Folder. Not Downloading");
                    conn.disconnect();
                    return false;
                }
            }
            File file = new File(folder, "app-update.apk");
            FileOutputStream fos = new FileOutputStream(file);
            Log.d("DL", "Connection done, File Obtained");
            ready = true;
            Log.d("DL", "Writing to file");
            float downloadSize = 0;
            int totalSize = conn.getContentLength();
            InputStream is = conn.getInputStream();
            byte[] buffer = new byte[1024];
            int len1;
            while ((len1 = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len1);
                downloadSize += len1;
                if (downloadSize % 80 == 0) {
                    Log.d("DL", "Download Size: " + downloadSize + "/" + totalSize);
                    publishProgress((downloadSize / totalSize) * 100, downloadSize, (float) totalSize);
                }
            }
            fos.close();
            is.close();//till here, it works fine - .apk is download to my sdcard in download file
            Log.d("DL", "Download Complete...");
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            except = e;
            return false;
        }
    }

    protected void onProgressUpdate(Float... progress){
        if (ready) {
            // Downloading new update... (Download Size / Total Size)
            double downloadMB = (double) Math.round((progress[1] / 1024.0 / 1024.0 *100)) / 100;
            double downloadSizeMB = (double) Math.round((progress[2] / 1024.0 / 1024.0 *100)) / 100;
            notification.setProgress(100, Math.round(progress[0]), false);
            notification.setContentText(activity.getString(R.string.notification_message_download_new_app_update, downloadMB, downloadSizeMB));
            manager.notify(notificationID, notification.build());
        } else {
            notification.setProgress(0, 0, true);
            manager.notify(notificationID, notification.build());
        }
    }

    @Override
    protected void onPostExecute(Boolean passed){
        Log.d("DL", "Processing download");
        notification.setAutoCancel(true).setOngoing(false);
        if (!passed){
            //Update failed, update notification
            if (except != null){
                //Print Exception
                notification.setContentTitle(activity.getString(R.string.notification_title_exception_download)).setTicker(activity.getString(R.string.notification_ticker_download_fail))
                        .setContentText(activity.getString(R.string.notification_content_download_fail_exception, except.getLocalizedMessage()))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(activity.getString(R.string.notification_content_download_fail_exception_expanded, except.getLocalizedMessage())))
                        .setSmallIcon(R.mipmap.ic_launcher).setProgress(0,0,false);
                Intent intent = new Intent(Intent.ACTION_VIEW, link);
                PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, 0);
                notification.setContentIntent(pendingIntent);
                manager.notify(notificationID, notification.build());
            } else {
                notification.setContentTitle(activity.getString(R.string.notification_title_exception_download)).setTicker(activity.getString(R.string.notification_ticker_download_fail))
                        .setContentText(activity.getString(R.string.notification_content_download_fail))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(activity.getString(R.string.notification_content_download_fail_expanded)))
                        .setSmallIcon(R.mipmap.ic_launcher).setProgress(0, 0, false);
                Intent intent = new Intent(Intent.ACTION_VIEW, link);
                PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, 0);
                notification.setContentIntent(pendingIntent);
                manager.notify(notificationID, notification.build());
            }
            return;
        }

        Log.d("DL", "Invoking Package Manager");
        //Invoke the Package Manager
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(filePATH + "app-update.apk")), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);

        //Notify User and add intent to invoke update
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentTitle(activity.getString(R.string.notification_title_download_success)).setTicker(activity.getString(R.string.notification_ticker_download_success))
                .setContentText(activity.getString(R.string.notification_content_download_success))
                .setAutoCancel(true).setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher).setProgress(0, 0, false);
        manager.notify(notificationID, notification.build());
    }

    private boolean tryAndCreateFolder(File folder){
        if (!folder.exists() || !folder.isDirectory()) {
            if (folder.isFile()) {
                //Rename it to something else
                int rename = 0;
                boolean check;
                do {
                    rename++;
                    check = folder.renameTo(new File(filePATH + "_" + rename));
                } while (!check);
                folder = new File(filePATH);
            }
            return folder.mkdir();
        }
        return false;
    }
}

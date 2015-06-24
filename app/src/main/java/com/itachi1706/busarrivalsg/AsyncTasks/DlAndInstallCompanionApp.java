package com.itachi1706.busarrivalsg.AsyncTasks;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.itachi1706.busarrivalsg.StaticVariables;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Kenneth on 24/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.AsyncTasks
 */
public class DlAndInstallCompanionApp extends AsyncTask<String, Void, Boolean> {

    private Activity activity;
    Exception except = null;
    private Uri link;
    private String filePath;

    public DlAndInstallCompanionApp(Activity activity){
        this.activity = activity;
    }

    @Override
    protected Boolean doInBackground(String... urlLink) {
        try {
            this.link = Uri.parse(urlLink[0]);
            URL url = new URL(urlLink[0]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(StaticVariables.HTTP_QUERY_TIMEOUT);
            conn.setReadTimeout(StaticVariables.HTTP_QUERY_TIMEOUT);
            conn.setRequestMethod("GET");
            conn.connect();
            Log.d("DL", "Starting Download");

            filePath = Environment.getExternalStorageDirectory() + File.separator + "SingBuses" + File.separator;

            Log.d("DL", "File Path: " + filePath);

            File folder = new File(filePath);
            if (!folder.exists()){
                if (!tryAndCreateFolder(folder)){
                    Log.d("Fail", "Cannot Create Folder. Not Downloading");
                    conn.disconnect();
                    return false;
                }
            }
            File file = new File(folder, "SingBuses.pbw");
            FileOutputStream fos = new FileOutputStream(file);
            Log.d("DL", "Connection done, File Obtained");
            Log.d("DL", "Writing to file");
            float downloadSize = 0;
            int totalSize = conn.getContentLength();
            InputStream is = conn.getInputStream();
            byte[] buffer = new byte[1024];
            int len1;
            while ((len1 = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len1);
                downloadSize += len1;
                Log.d("DL", "Download Size: " + downloadSize + "/" + totalSize);
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

    @Override
    protected void onPostExecute(Boolean passed){
        if (!passed){
            if (except != null){
                Toast.makeText(activity.getApplicationContext(), "An Error Occurred (" + except.getMessage() + ")", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity.getApplicationContext(), "An Error Occured while downloading the latest version of the Pebble App", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        Intent installCompanionApp = new Intent(Intent.ACTION_VIEW);
        installCompanionApp.setDataAndType(Uri.fromFile(new File(filePath + "SingBuses.pbw")), "application/octet-stream");
        installCompanionApp.setComponent(new ComponentName("com.getpebble.android", "com.getpebble.android.ui.UpdateActivity"));
        //installCompanionApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(installCompanionApp);
    }

    private boolean tryAndCreateFolder(File folder){
        if (!folder.exists() || !folder.isDirectory()) {
            if (folder.isFile()) {
                //Rename it to something else
                int rename = 0;
                boolean check;
                do {
                    rename++;
                    check = folder.renameTo(new File(filePath + "_" + rename));
                } while (!check);
                folder = new File(filePath);
            }
            return folder.mkdir();
        }
        return false;
    }
}

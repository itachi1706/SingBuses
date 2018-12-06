package com.itachi1706.busarrivalsg.Util;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.itachi1706.busarrivalsg.objects.gson.ntubuses.NTUBus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import androidx.annotation.Nullable;

/**
 * Created by Kenneth on 6/12/2018.
 * for com.itachi1706.busarrivalsg.Util in SingBuses
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class NTURouteCacher {

    private Context mContext;
    private static String TAG = "NTU-ROUTE-CACHE";

    public NTURouteCacher(Context context) {
        this.mContext = context;
    }

    private void init() {
        File directory = getDirectory();
        if (!directory.exists() || (directory.exists() && directory.isFile() && directory.delete())) directory.mkdir();
    }

    private File getDirectory() {
        return new File(mContext.getCacheDir(), "ntu");
    }

    private boolean hasCachedFile(String routeCode) {
        // Check for cached file as well as if it has expired or not
        File f = getFileObject(routeCode);
        if (!f.exists()) return false;

        Date lastModified = new Date(f.lastModified());
        Log.i(TAG, "Found cache for Route " + routeCode + ", last updated on " + lastModified.toString());

        // Check if greater than cache (1 week)
        long currentTime = System.currentTimeMillis();
        long fileTime = lastModified.getTime();
        long diff = currentTime - fileTime;
        if (diff > (7 * 24 * 60 * 60 * 1000)) {
            f.delete();
            return false;
        }
        return true;
    }

    private File getFileObject(String routeCode) {
        return new File(getDirectory().getAbsolutePath() + "/ntu-route-" + routeCode + ".json");
    }

    @Nullable
    private File getCachedFile(String routeCode) {
        if (hasCachedFile(routeCode)) {
            return getFileObject(routeCode);
        }
        return null;
    }

    public String getCachedRoute(String routeCode) {
        File f = getCachedFile(routeCode);
        if (f == null) return null;

        // Read text file and return it
        StringBuilder sb = new StringBuilder();
        try {
            InputStream is = new FileInputStream(f);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "Cannot parse file, assuming corrupted");
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Cannot parse file, assuming corrupted");
            return null;
        }
        return sb.toString();
    }

    public boolean writeCachedRoute(String routeCode, NTUBus.MapRouting route) {
        String routeString = getStringFromRoute(route);
        return writeCachedRoute(routeCode, routeString);
    }

    private boolean writeCachedRoute(String routeCode, String routeData) {
        File f = getFileObject(routeCode);
        if (f.exists()) {
            if (!f.delete()) {
                Log.e(TAG, "Unable to remove old cache. Not proceeding");
                return false;
            }
        }

        try {
            f.createNewFile();
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(routeData.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "Cannot write cache, assuming cache write fail");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "IO Exception writing cache, assuming write fail");
            return false;
        }
        return true;
    }

    public NTUBus.MapRouting getRouteFromString(String routeString) {
        Gson gson = new Gson();
        return gson.fromJson(routeString, NTUBus.MapRouting.class);
    }

    private String getStringFromRoute(NTUBus.MapRouting routes) {
        Gson gson = new Gson();
        return gson.toJson(routes);
    }
}

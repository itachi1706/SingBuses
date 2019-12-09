package com.itachi1706.busarrivalsg.AsyncTasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;

import com.google.gson.Gson;
import com.itachi1706.appupdater.Util.DeprecationHelper;
import com.itachi1706.appupdater.Util.URLHelper;
import com.itachi1706.busarrivalsg.objects.gson.ntubuses.NTUBusTimings;
import com.itachi1706.busarrivalsg.util.StaticVariables;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by Kenneth on 9/12/2019.
 * for com.itachi1706.busarrivalsg.AsyncTasks in SingBuses
 */
public class QueryNTUStops extends AsyncTask<Integer, Void, Void> {

    private WeakReference<Activity> actRef;
    private Callback callback;
    private String originalSubtext;

    public QueryNTUStops(Activity activity, String subtext, Callback onCompleteCallback) {
        this.actRef = new WeakReference<>(activity);
        this.originalSubtext = subtext;
        this.callback = onCompleteCallback;
    }

    @Override
    protected Void doInBackground(Integer... stopIds) {
        Activity activity = actRef.get();
        if (activity == null) return null;
        int stopId = stopIds[0];
        String url = "https://api.itachi1706.com/api/ntubus.php?busarrival=true&stopid=" + stopId;
        String TAG = "QueryNTU";
        Log.d(TAG, url);
        String tmp;
        try {
            long start = System.currentTimeMillis();
            URLHelper urlHelper = new URLHelper(url);
            tmp = urlHelper.executeString();
            Log.i(TAG, "Data retrieved in " + (System.currentTimeMillis() - start) + "ms");
        } catch (IOException e) {
            activity.runOnUiThread(() -> {
                // Update error
                callback.onComplete(true, "An I/O Exception has occurred. Please try again later\n\n" + e.getLocalizedMessage(), null, null);
            });
            e.printStackTrace();
            return null;
        }

        if (!StaticVariables.INSTANCE.checkIfYouGotJsonString(tmp)) {
            Log.e(TAG, "Error JSON: " + tmp);
            activity.runOnUiThread(() -> {
                // Update error
                callback.onComplete(true, "An error has occurred retrieving data from the API. Please try again later", null, null);
            });
            return null;
        }

        Gson gson = new Gson();
        NTUBusTimings t = gson.fromJson(tmp, NTUBusTimings.class);

        // Craft timings screen
        StringBuilder sb = new StringBuilder();
        ArrayMap<Integer, String> tmgs = new ArrayMap<>();
        if (t.getForecast() == null || t.getForecast().length <= 0) sb.append("No Timings found");
        else {
            for (NTUBusTimings.Forecast f : t.getForecast()) {
                assert f.getRoute() != null;
                double sec = f.getForecast_seconds();
                String timeString = getRouteColorHtml(f.getRv_id(), f.getRoute().getShort_name()) + ":\t";
                if (tmgs.containsKey(f.getRv_id()))
                    timeString = tmgs.get(f.getRv_id());
                if (sec > 60) {
                    // Call in minutes
                    int min = (int) (sec / 60);
                    timeString += min + ((min > 1) ? " mins" : " min");
                } else if (sec <= 0) {
                    timeString += "Arriving";
                } else {
                    int seci = (int) sec;
                    timeString += seci + ((seci > 1) ? " secs" : " sec");
                }
                timeString += ", ";
                tmgs.put(f.getRv_id(), timeString);
            }

            if (tmgs.size() > 0) {
                for (ArrayMap.Entry<Integer, String> pair : tmgs.entrySet()) {
                    String ts = pair.getValue().replaceAll(", $", "");
                    sb.append(ts).append("\n");
                }
            }
        }

        String parsedString = sb.toString().replace("\t", "&nbsp;&nbsp;").replace("\n", "<br/>");

        activity.runOnUiThread(() -> {
            // Update basically everything as well
            callback.onComplete(false, DeprecationHelper.Html.fromHtml(parsedString).toString(), t.getName(), originalSubtext + "\nParsed Stop ID: " + t.getId());
        });
        return null;
    }

    private String getRouteColorHtml(int id, String data) {
        switch (id) {
            case 44478: return "<font color=\"red\">" + data + "</font>";
            case 44479: return "<font color=\"blue\">" + data + "</font>";
            case 44480: return "<font color=\"#006400\">" + data + "</font>";
            case 44481: return "<font color=\"#964B00\">" + data + "</font>";
            case 199179: return "<font color=\"#800080\">" + data + "</font>"; // SBST
            default: return "<font color=\"black\">" + data + "</font>";
        }
    }

    public interface Callback {
        void onComplete(boolean error, String resultText, @Nullable String title, @Nullable String subtext);
    }

}

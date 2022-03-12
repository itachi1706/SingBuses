package com.itachi1706.busarrivalsg.AsyncTasks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.itachi1706.busarrivalsg.NTUBusActivity;
import com.itachi1706.busarrivalsg.R;
import com.itachi1706.busarrivalsg.objects.gson.ntubuses.NTUBus;
import com.itachi1706.busarrivalsg.util.NTURouteCacher;
import com.itachi1706.busarrivalsg.util.StaticVariables;
import com.itachi1706.helperlib.concurrent.CoroutineAsyncTask;
import com.itachi1706.helperlib.helpers.LogHelper;
import com.itachi1706.helperlib.helpers.URLHelper;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;

/**
 * Created by Kenneth on 07/9/2018
 * for SingBuses in package com.itachi1706.busarrivalsg.AsyncTasks
 */
public class GetNTUData extends CoroutineAsyncTask<String, Void, Integer> {

    private final WeakReference<Activity> activityRef;
    private Exception except;
    private int update;
    private static final String TAG = "NTUData";
    private static final String TASK_NAME = GetNTUData.class.getSimpleName();

    public GetNTUData(Activity activity, boolean update) {
        super(TASK_NAME);
        this.activityRef = new WeakReference<>(activity);
        this.update = (update) ? 1 : 0;
    }

    @Override
    public Integer doInBackground(String... routes) {
        StringBuilder routeString = new StringBuilder();
        if (routes.length == 1) {
            routeString = new StringBuilder(routes[0]);
        } else {
            for (String r : routes) {
                routeString.append(r).append(",");
            }
            routeString = new StringBuilder(routeString.toString().replaceAll(",$", ""));
        }
        Activity mActivity = activityRef.get();
        boolean fakeUpdate = false;
        NTURouteCacher cacheHelper = new NTURouteCacher(mActivity);
        if (update != 1) {
            // Check for cache. if 1 is not cached all are not
            fakeUpdate = true;
            update = 1;
            for (String r : routes) {
                if (!cacheHelper.hasCachedFile(r)) {
                    fakeUpdate = false;
                    update = 0;
                }
            }
        }
        String url = "https://api.itachi1706.com/api/ntubus.php?route=" + routeString.toString() + "&update=" + update;
        LogHelper.d(TAG, url);
        String tmp;
        try {
            long start = System.currentTimeMillis();
            URLHelper urlHelper = new URLHelper(url);
            tmp = urlHelper.executeString();
            LogHelper.i(TAG, "Data retrieved in " + (System.currentTimeMillis() - start) + "ms");
        } catch (IOException e) {
            except = e;
            return 1;
        }

        LogHelper.d(TAG, (tmp == null) ? "null" : tmp);
        if (tmp == null || !StaticVariables.INSTANCE.checkIfYouGotJsonString(tmp)) {
            except = new Exception(mActivity.getResources().getString(R.string.toast_message_invalid_json));
            return 2;
        }

        Gson gson = new Gson();
        NTUBus b = gson.fromJson(tmp, NTUBus.class);
        if (b.getRoutes() != null) {
            for (NTUBus.Route r : b.getRoutes()) {
                if (fakeUpdate) {
                    // Get route from cache
                    String rtS = cacheHelper.getCachedRoute(cacheHelper.getRouteCode(r.getId()));
                    if (rtS != null) {
                        NTUBus.MapRouting route = cacheHelper.getRouteFromString(rtS);
                        r.setRoute(route);
                    } else {
                        // Present error
                        Intent sendForMapParsingIntent = new Intent(NTUBusActivity.RECEIVE_NTU_DATA_EVENT);
                        sendForMapParsingIntent.putExtra("err", true);
                        mActivity.runOnUiThread(() -> LocalBroadcastManager.getInstance(mActivity).sendBroadcast(sendForMapParsingIntent));
                        return 0;
                    }
                } else if (update != 1) {
                    // Write route to cache
                    cacheHelper.writeCachedRoute(cacheHelper.getRouteCode(r.getId()), r.getRoute());
                }
            }
        }
        if (fakeUpdate) {
            // Write back to tmp
            tmp = gson.toJson(b);
            update = 0;
        }

        Intent sendForMapParsingIntent = new Intent(NTUBusActivity.RECEIVE_NTU_DATA_EVENT);
        sendForMapParsingIntent.putExtra("data", tmp);
        sendForMapParsingIntent.putExtra("update", update);
        mActivity.runOnUiThread(() -> LocalBroadcastManager.getInstance(mActivity).sendBroadcast(sendForMapParsingIntent));
        return 0;
    }

    public void onPostExecute(Integer errorCode) {
        Context context = activityRef.get();
        if (except != null && errorCode != 0) {
            LogHelper.e(TAG, "Exception occurred (" + except.getMessage() + ")");
            if (except instanceof SocketTimeoutException) {
                Toast.makeText(context, "NTU API did not respond in a timely manner", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, except.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}

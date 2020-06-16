package com.itachi1706.busarrivalsg.AsyncTasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.itachi1706.busarrivalsg.R;
import com.itachi1706.busarrivalsg.util.StaticVariables;
import com.itachi1706.helperlib.helpers.URLHelper;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;

/**
 * Created by Kenneth on 20/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.AsyncTasks
 */
public class GetBusServicesHandler extends AsyncTask<String, Void, String> {

    private ProgressDialog dialog;
    private WeakReference<Activity> actRef;
    private Exception exception = null;

    private Handler mHandler;

    public GetBusServicesHandler(ProgressDialog dialog, Activity activity, Handler handler){
        this.dialog = dialog;
        this.actRef = new WeakReference<>(activity);
        this.mHandler = handler;
    }

    @Override
    protected String doInBackground(String... busCodes) {
        String busCode = busCodes[0];
        String url = "https://api.itachi1706.com/api/busarrival.php?BusStopCode=" + busCode + "&api=2";
        String tmp = "";

        Log.d("GET-BUS-SERVICE", url);
        try {
            URLHelper urlHelper = new URLHelper(url);
            tmp = urlHelper.executeString();
        } catch (IOException e) {
            exception = e;
        }
        return tmp;
    }

    protected void onPostExecute(String json){
        if (exception != null){
            Activity activity = actRef.get();
            if (activity == null) return;
            if (exception instanceof SocketTimeoutException) {
                Toast.makeText(activity, R.string.toast_message_timeout_request, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
            if (!(activity.isFinishing() || activity.isChangingConfigurations()))
                dialog.dismiss();
        } else {
            //Go parse it
            Message msg = Message.obtain();
            msg.what = StaticVariables.BUS_SERVICE_JSON_RETRIEVED;
            Bundle bundle = new Bundle();
            bundle.putString("jsonString", json);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
            dialog.dismiss();
        }
    }
}

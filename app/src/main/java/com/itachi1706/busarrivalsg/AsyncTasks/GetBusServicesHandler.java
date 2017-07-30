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
import com.itachi1706.busarrivalsg.Util.StaticVariables;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Created by Kenneth on 20/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.AsyncTasks
 */
public class GetBusServicesHandler extends AsyncTask<String, Void, String> {

    private ProgressDialog dialog;
    private Activity activity;
    private Exception exception = null;

    private Handler mHandler;

    public GetBusServicesHandler(ProgressDialog dialog, Activity activity, Handler handler){
        this.dialog = dialog;
        this.activity = activity;
        this.mHandler = handler;
    }

    @Override
    protected String doInBackground(String... busCodes) {
        String busCode = busCodes[0];
        String url = "http://api.itachi1706.com/api/busarrival.php?BusStopID=" + busCode + "&api=2";
        String tmp = "";

        Log.d("GET-BUS-SERVICE", url);
        try {
            URL urlConn = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlConn.openConnection();
            conn.setConnectTimeout(StaticVariables.HTTP_QUERY_TIMEOUT);
            conn.setReadTimeout(StaticVariables.HTTP_QUERY_TIMEOUT);
            InputStream in = conn.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder str = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null)
            {
                str.append(line);
            }
            in.close();
            tmp = str.toString();
        } catch (IOException e) {
            exception = e;
        }
        return tmp;
    }

    protected void onPostExecute(String json){
        if (exception != null){
            if (exception instanceof SocketTimeoutException) {
                Toast.makeText(activity, R.string.toast_message_timeout_request, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
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

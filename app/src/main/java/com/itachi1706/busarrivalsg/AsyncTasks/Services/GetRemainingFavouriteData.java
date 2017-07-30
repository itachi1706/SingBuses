package com.itachi1706.busarrivalsg.AsyncTasks.Services;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusArrivalArrayObject;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusArrivalMain;
import com.itachi1706.busarrivalsg.Objects.BusServices;
import com.itachi1706.busarrivalsg.Objects.BusStatus;
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
public class GetRemainingFavouriteData extends AsyncTask<BusServices, Void, String> {

    private Exception exception = null;

    private BusServices busObj;

    public GetRemainingFavouriteData(){}

    @Override
    protected String doInBackground(BusServices... busObject) {
        this.busObj = busObject[0];
        String url = "http://api.itachi1706.com/api/busarrival.php?BusStopID=" + this.busObj.getStopID() + "&ServiceNo=" + this.busObj.getServiceNo() + "&api=2";
        String tmp = "";

        Log.d("GET-FAV-BUS-SERVICE", url);
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
                Log.e("PebbleComm Fav", "Request Timed Out, Retrying");
                new GetRemainingFavouriteData().executeOnExecutor(THREAD_POOL_EXECUTOR, busObj);
            } else {
                Log.e("PebbleComm Fav", exception.getMessage());
            }
        } else {
            //Go parse it
            Gson gson = new Gson();
            if (!StaticVariables.checkIfYouGotJsonString(json)){
                //Invalid string, retrying
                Log.e("PebbleComm Fav", "Invalid JSON String, Retrying");
                new GetRemainingFavouriteData().executeOnExecutor(THREAD_POOL_EXECUTOR, busObj);
                return;
            }

            BusArrivalMain mainArr = gson.fromJson(json, BusArrivalMain.class);
            BusArrivalArrayObject[] array = mainArr.getServices();

            if (array.length == 0 || array.length > 1){
                Log.e("PebbleComm Fav", "A weird error occured. It seems that the array received had a size of " + array.length);
                if (array.length == 0) {
                    Log.e("PebbleComm Fav", "Retrying...");
                    new GetRemainingFavouriteData().executeOnExecutor(THREAD_POOL_EXECUTOR, busObj);
                    return;
                } else {
                    Log.e("PebbleComm Fav", "Gonna use first data");
                }
            }
            Log.i("PebbleComm Fav", "Processing " + busObj.getServiceNo() + " at code " + busObj.getStopID());
            //Assuming One
            BusArrivalArrayObject item = array[0];

            BusStatus nextBus = new BusStatus();
            nextBus.setEstimatedArrival(item.getNextBus().getEstimatedArrival());
            nextBus.setIsWheelChairAccessible(item.getNextBus().getFeature());
            nextBus.setLoad(item.getNextBus().getLoad());

            BusStatus subsequentBus = new BusStatus();
            subsequentBus.setEstimatedArrival(item.getNextBus2().getEstimatedArrival());
            subsequentBus.setIsWheelChairAccessible(item.getNextBus2().getFeature());
            subsequentBus.setLoad(item.getNextBus2().getLoad());

            busObj.setCurrentBus(nextBus);
            busObj.setNextBus(subsequentBus);
            busObj.setTime(System.currentTimeMillis());
            busObj.setObtainedNextData(true);

            //Go through arrayList and update the current one
            for (int i = 0; i < StaticVariables.favouritesList.size(); i++){
                BusServices ob = StaticVariables.favouritesList.get(i);
                if (ob.getServiceNo().equals(busObj.getServiceNo()) && ob.getStopID().equals(busObj.getStopID())){
                    //Update
                    StaticVariables.favouritesList.set(i, busObj);
                    return;
                }
            }
        }
    }
}

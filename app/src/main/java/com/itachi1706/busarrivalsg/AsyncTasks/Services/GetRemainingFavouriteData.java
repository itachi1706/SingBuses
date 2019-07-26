package com.itachi1706.busarrivalsg.AsyncTasks.Services;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.itachi1706.appupdater.Util.URLHelper;
import com.itachi1706.busarrivalsg.gsonObjects.sgLTA.BusArrivalArrayObject;
import com.itachi1706.busarrivalsg.gsonObjects.sgLTA.BusArrivalMain;
import com.itachi1706.busarrivalsg.objects.BusServices;
import com.itachi1706.busarrivalsg.objects.BusStatus;
import com.itachi1706.busarrivalsg.util.StaticVariables;

import java.io.IOException;
import java.net.SocketTimeoutException;

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
        String url = "https://api.itachi1706.com/api/busarrival.php?BusStopCode=" + this.busObj.getStopID() + "&ServiceNo=" + this.busObj.getServiceNo() + "&api=2";
        String tmp = "";

        Log.d("GET-FAV-BUS-SERVICE", url);
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
            if (exception instanceof SocketTimeoutException) {
                Log.e("PebbleComm Fav", "Request Timed Out, Retrying");
                new GetRemainingFavouriteData().executeOnExecutor(THREAD_POOL_EXECUTOR, busObj);
            } else {
                Log.e("PebbleComm Fav", exception.getMessage());
            }
        } else {
            //Go parse it
            Gson gson = new Gson();
            if (!StaticVariables.INSTANCE.checkIfYouGotJsonString(json)){
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
            for (int i = 0; i < StaticVariables.INSTANCE.getFavouritesList().size(); i++){
                BusServices ob = StaticVariables.INSTANCE.getFavouritesList().get(i);
                if (ob.getServiceNo().equals(busObj.getServiceNo()) && ob.getStopID().equals(busObj.getStopID())){
                    //Update
                    StaticVariables.INSTANCE.getFavouritesList().set(i, busObj);
                    return;
                }
            }
        }
    }
}

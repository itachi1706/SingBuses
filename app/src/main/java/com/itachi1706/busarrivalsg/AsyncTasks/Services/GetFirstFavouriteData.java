package com.itachi1706.busarrivalsg.AsyncTasks.Services;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.google.gson.Gson;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusArrivalArrayObject;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusArrivalMain;
import com.itachi1706.busarrivalsg.Objects.BusServices;
import com.itachi1706.busarrivalsg.Objects.BusStatus;
import com.itachi1706.busarrivalsg.Util.PebbleEnum;
import com.itachi1706.busarrivalsg.Util.StaticVariables;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import static com.itachi1706.busarrivalsg.Util.StaticVariables.parseEstimateArrival;

/**
 * Created by Kenneth on 20/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.AsyncTasks
 */
public class GetFirstFavouriteData extends AsyncTask<BusServices, Void, String> {

    private Exception exception = null;
    private Context context;

    private BusServices busObj;

    public GetFirstFavouriteData(Context context){
        this.context = context;
    }

    @Override
    protected String doInBackground(BusServices... busObject) {
        this.busObj = busObject[0];
        String url = "http://api.itachi1706.com/api/busarrival.php?BusStopID=" + this.busObj.getStopID() + "&ServiceNo=" + this.busObj.getServiceNo() + "&api=2";
        String tmp = "";

        Log.d("GET-FIRST-BUS-SERVICE", url);
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

    protected void onPostExecute(String json) {
        if (exception != null) {
            if (exception instanceof SocketTimeoutException) {
                Log.e("PebbleComm First Fav", "Request Timed Out, Retrying");
                new GetFirstFavouriteData(context).executeOnExecutor(THREAD_POOL_EXECUTOR, busObj);
            } else {
                Log.e("PebbleComm First Fav", exception.getMessage());
            }
        } else {
            //Go parse it
            Gson gson = new Gson();
            if (!StaticVariables.checkIfYouGotJsonString(json)) {
                //Invalid string, retrying
                Log.e("PebbleComm First Fav", "Invalid JSON String, Retrying");
                new GetFirstFavouriteData(context).executeOnExecutor(THREAD_POOL_EXECUTOR, busObj);
                return;
            }

            BusArrivalMain mainArr = gson.fromJson(json, BusArrivalMain.class);
            BusArrivalArrayObject[] array = mainArr.getServices();

            if (array.length == 0 || array.length > 1) {
                Log.e("PebbleComm First Fav", "A weird error occured. It seems that the array received had a size of " + array.length);
                if (array.length == 0) {
                    Log.e("PebbleComm First Fav", "Retrying...");
                    new GetFirstFavouriteData(context).executeOnExecutor(THREAD_POOL_EXECUTOR, busObj);
                    return;
                } else {
                    Log.e("PebbleComm First Fav", "Gonna use first data");
                }
            }
            //Assuming One
            Log.i("PebbleComm First Fav", "Processing " + busObj.getServiceNo() + " at code " + busObj.getStopID());
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
            for (int i = 0; i < StaticVariables.favouritesList.size(); i++) {
                BusServices ob = StaticVariables.favouritesList.get(i);
                if (ob.getServiceNo().equals(busObj.getServiceNo()) && ob.getStopID().equals(busObj.getStopID())) {
                    //Update
                    StaticVariables.favouritesList.set(i, busObj);
                    break;
                }
            }

            //Get First Object and parse it
            BusServices ob = StaticVariables.favouritesList.get(0);
            String currentEst;
            String nextEst;
            if (ob.getNextBus().getEstimatedArrival() == null) {
                nextEst = "-";
            } else {
                long estNxt = parseEstimateArrival(ob.getNextBus().getEstimatedArrival());
                if (estNxt <= 0)
                    nextEst = "Arr";
                else if (estNxt == 1)
                    nextEst = estNxt + " min";
                else
                    nextEst = estNxt + " mins";
            }
            if (ob.getCurrentBus().getEstimatedArrival() == null) {
                currentEst = "-";
            } else {
                long estCur = parseEstimateArrival(ob.getCurrentBus().getEstimatedArrival());
                if (estCur <= 0)
                    currentEst = "Arr";
                else if (estCur == 1)
                    currentEst = estCur + " min";
                else
                    currentEst = estCur + " mins";
            }

            //Push first to pebble
            PebbleDictionary dict1 = new PebbleDictionary();
            PebbleDictionary dict2 = new PebbleDictionary();
            PebbleDictionary dict3 = new PebbleDictionary();
            dict1.addInt16(PebbleEnum.MESSAGE_DATA_EVENT, (short) 1);
            dict2.addInt16(PebbleEnum.MESSAGE_DATA_EVENT, (short) 2);
            dict3.addInt16(PebbleEnum.MESSAGE_DATA_EVENT, (short) 3);
            if (ob.getStopName() == null)
                dict1.addString(PebbleEnum.MESSAGE_ROAD_NAME, "Unknown Stop");
            else
                dict1.addString(PebbleEnum.MESSAGE_ROAD_NAME, ob.getStopName().trim());
            dict2.addString(PebbleEnum.MESSAGE_BUS_SERVICE, ob.getServiceNo().trim());
            dict2.addString(PebbleEnum.MESSAGE_ROAD_CODE, ob.getStopID().trim());
            dict2.addInt16(PebbleEnum.MESSAGE_MAX_FAV, (short) StaticVariables.favouritesList.size());
            dict2.addInt16(PebbleEnum.MESSAGE_CURRENT_FAV, (short) 1);
            dict3.addString(PebbleEnum.ESTIMATE_ARR_CURRENT_DATA, currentEst.trim());
            dict3.addInt16(PebbleEnum.ESTIMATE_LOAD_CURRENT_DATA, (short) ob.getCurrentBus().getLoad());
            dict3.addString(PebbleEnum.ESTIMATE_ARR_NEXT_DATA, nextEst.trim());
            dict3.addInt16(PebbleEnum.ESTIMATE_LOAD_NEXT_DATA, (short) ob.getNextBus().getLoad());

            //Send WAB status
            dict2.addInt8(PebbleEnum.MESSAGE_WAB_CURRENT, StaticVariables.parseWABStatusToPebble(ob.getCurrentBus().isWheelChairAccessible()));
            dict2.addInt8(PebbleEnum.MESSAGE_WAB_NEXT, StaticVariables.parseWABStatusToPebble(ob.getNextBus().isWheelChairAccessible()));

            Log.i("PebbleComm First Fav", "Sending to Pebble...");
            PebbleKit.sendDataToPebbleWithTransactionId(context, StaticVariables.PEBBLE_APP_UUID, dict1, 1);
            StaticVariables.dict1 = dict1;
            StaticVariables.dict2 = dict2;
            StaticVariables.dict3 = dict3;
        }
    }
}

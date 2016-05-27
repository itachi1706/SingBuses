package com.itachi1706.busarrivalsg.AsyncTasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusArrivalArrayObject;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusArrivalMain;
import com.itachi1706.busarrivalsg.Objects.BusServices;
import com.itachi1706.busarrivalsg.Objects.BusStatus;
import com.itachi1706.busarrivalsg.R;
import com.itachi1706.busarrivalsg.RecyclerViews.FavouritesRecyclerAdapter;
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
public class GetBusServicesFavouritesRecycler extends AsyncTask<BusServices, Void, String> {

    private Activity activity;
    private Exception exception = null;
    private FavouritesRecyclerAdapter adapter;

    private BusServices busObj;

    public GetBusServicesFavouritesRecycler(Activity activity, FavouritesRecyclerAdapter adapter){
        this.activity = activity;
        this.adapter = adapter;
    }

    @Override
    protected String doInBackground(BusServices... busObject) {
        this.busObj = busObject[0];
        String url = "http://api.itachi1706.com/api/busarrival.php?BusStopID=" + this.busObj.getStopID() + "&ServiceNo=" + this.busObj.getServiceNo();
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
                Toast.makeText(activity, R.string.toast_message_timeout_request_retry, Toast.LENGTH_SHORT).show();
                new GetBusServicesFavouritesRecycler(activity,adapter).executeOnExecutor(THREAD_POOL_EXECUTOR, busObj);
            } else {
                Toast.makeText(activity, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            //Go parse it
            Gson gson = new Gson();
            if (!StaticVariables.checkIfYouGotJsonString(json)){
                //Invalid string, retrying
                Toast.makeText(activity, R.string.toast_message_invalid_json_retry, Toast.LENGTH_SHORT).show();
                new GetBusServicesFavouritesRecycler(activity,adapter).executeOnExecutor(THREAD_POOL_EXECUTOR, busObj);
                return;
            }

            BusArrivalMain mainArr = gson.fromJson(json, BusArrivalMain.class);

            boolean jsonError = false;
            if (mainArr == null) jsonError = true;
            if (!jsonError && mainArr.getServices() == null) jsonError = true;

            if (jsonError){
                Log.e("FAV-GET", "Retrying...");
                new GetBusServicesFavouritesRecycler(activity,adapter).executeOnExecutor(THREAD_POOL_EXECUTOR, busObj);
                return;
            }

            BusArrivalArrayObject[] array = mainArr.getServices();

            if (array.length == 0 || array.length > 1){
                Log.e("FAV-GET", "A weird error occurred. It seems that the array received had a size of " + array.length);
                if (array.length == 0) {
                    Log.e("FAV-GET", "Retrying...");
                    new GetBusServicesFavouritesRecycler(activity,adapter).executeOnExecutor(THREAD_POOL_EXECUTOR, busObj);
                    return;
                } else {
                    Log.e("FAV-GET", "Gonna use first data");
                }
            }
            //Assuming One
            BusArrivalArrayObject item = array[0];

            BusStatus nextBus = new BusStatus();
            nextBus.setEstimatedArrival(item.getNextBus().getEstimatedArrival());
            nextBus.setIsWheelChairAccessible(item.getNextBus().getFeature());
            nextBus.setLoad(item.getNextBus().getLoad());

            BusStatus subsequentBus = new BusStatus();
            subsequentBus.setEstimatedArrival(item.getSubsequentBus().getEstimatedArrival());
            subsequentBus.setIsWheelChairAccessible(item.getSubsequentBus().getFeature());
            subsequentBus.setLoad(item.getSubsequentBus().getLoad());

            if (item.getSubsequentBus3() != null){
                //New API in place

                //First add the required stuff to the other 2
                nextBus.setVisitNumber(item.getNextBus().getVisitNumber());
                nextBus.setLatitude(item.getNextBus().getLatitude());
                nextBus.setLongitude(item.getNextBus().getLongitude());

                subsequentBus.setVisitNumber(item.getSubsequentBus().getVisitNumber());
                subsequentBus.setLatitude(item.getSubsequentBus().getLatitude());
                subsequentBus.setLongitude(item.getSubsequentBus().getLongitude());


                BusStatus subsequent2Bus = new BusStatus();
                subsequent2Bus.setEstimatedArrival(item.getSubsequentBus3().getEstimatedArrival());
                subsequent2Bus.setIsWheelChairAccessible(item.getSubsequentBus3().getFeature());
                subsequent2Bus.setLoad(item.getSubsequentBus3().getLoad());
                subsequent2Bus.setVisitNumber(item.getSubsequentBus3().getVisitNumber());
                subsequent2Bus.setLatitude(item.getSubsequentBus3().getLatitude());
                subsequent2Bus.setLongitude(item.getSubsequentBus3().getLongitude());

                busObj.setSubsequentBus(subsequent2Bus);
            }


            busObj.setCurrentBus(nextBus);
            busObj.setNextBus(subsequentBus);
            busObj.setTime(System.currentTimeMillis());
            busObj.setOperatingStatus(item.getStatus());
            busObj.setObtainedNextData(true);

            //Go through arrayList and update the current one
            for (int i = 0; i < StaticVariables.favouritesList.size(); i++){
                BusServices ob = StaticVariables.favouritesList.get(i);
                if (ob.getServiceNo().equals(busObj.getServiceNo()) && ob.getStopID().equals(busObj.getStopID())){
                    //Update
                    StaticVariables.favouritesList.set(i, busObj);
                    adapter.updateAdapter(StaticVariables.favouritesList);
                    adapter.notifyDataSetChanged();
                    return;
                }
            }

        }
    }
}

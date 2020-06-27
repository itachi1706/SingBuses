package com.itachi1706.busarrivalsg.AsyncTasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.gson.Gson;
import com.itachi1706.busarrivalsg.R;
import com.itachi1706.busarrivalsg.RecyclerViews.FavouritesRecyclerAdapter;
import com.itachi1706.busarrivalsg.gsonObjects.sgLTA.BusArrivalArrayObject;
import com.itachi1706.busarrivalsg.gsonObjects.sgLTA.BusArrivalMain;
import com.itachi1706.busarrivalsg.objects.BusServices;
import com.itachi1706.busarrivalsg.objects.BusStatus;
import com.itachi1706.busarrivalsg.util.StaticVariables;
import com.itachi1706.helperlib.helpers.LogHelper;
import com.itachi1706.helperlib.helpers.URLHelper;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.util.Objects;

/**
 * Created by Kenneth on 20/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.AsyncTasks
 */
public class GetBusServicesFavouritesRecycler extends AsyncTask<BusServices, Void, String> {

    private final WeakReference<Activity> actRef;
    private Exception exception = null;
    private final FavouritesRecyclerAdapter adapter;

    private BusServices[] busObjArr;

    public GetBusServicesFavouritesRecycler(Activity activity, FavouritesRecyclerAdapter adapter){
        this.actRef = new WeakReference<>(activity);
        this.adapter = adapter;
    }

    @Override
    protected String doInBackground(BusServices... busObject) {
        // Create the bus obj string
        StringBuilder sb = new StringBuilder();
        for (BusServices s : busObject) {
            sb.append(s.getStopID()).append(":").append(s.getServiceNo()).append(";");
        }
        String csv = sb.toString();
        csv = csv.substring(0, csv.length() - 1);
        this.busObjArr = busObject;

        String url = "https://api.itachi1706.com/api/busarrival.php?CSV=" + csv + "&api=2";
        String tmp = "";
        URLHelper urlHelper = new URLHelper(url);

        LogHelper.d("GET-FAV-BUS-SERVICE", url);
        try {
            tmp = urlHelper.executeString();
        } catch (IOException e) {
            exception = e;
        }
        return tmp;
    }

    protected void onPostExecute(String json){
        Activity activity = actRef.get();
        if (activity == null) return; // NO-OP
        if (exception != null){
            if (exception instanceof SocketTimeoutException) {
                Toast.makeText(activity, R.string.toast_message_timeout_request_retry, Toast.LENGTH_SHORT).show();
                new GetBusServicesFavouritesRecycler(activity,adapter).executeOnExecutor(THREAD_POOL_EXECUTOR, busObjArr);
            } else {
                Toast.makeText(activity, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            //Go parse it
            Gson gson = new Gson();
            if (!StaticVariables.INSTANCE.checkIfYouGotJsonString(json)){
                //Invalid string, retrying
                Toast.makeText(activity, R.string.toast_message_invalid_json_retry, Toast.LENGTH_SHORT).show();
                new GetBusServicesFavouritesRecycler(activity,adapter).executeOnExecutor(THREAD_POOL_EXECUTOR, busObjArr);
                return;
            }

            BusArrivalMain[] mainArrs = gson.fromJson(json, BusArrivalMain[].class);

            boolean jsonError = false;
            if (mainArrs == null) jsonError = true;
            if (!jsonError && (mainArrs[0] == null || mainArrs[0].getServices() == null)) jsonError = true;

            if (jsonError){
                LogHelper.e("FAV-GET", "Retrying...");
                new GetBusServicesFavouritesRecycler(activity,adapter).executeOnExecutor(THREAD_POOL_EXECUTOR, busObjArr);
                return;
            }

            for (BusArrivalMain mainArr : mainArrs) {
                BusArrivalArrayObject[] array = mainArr.getServices();

                //Assuming One
                assert array != null;
                BusArrivalArrayObject item = array[0];
                assert item.getNextBus() != null;
                assert item.getNextBus2() != null;
                assert item.getNextBus3() != null;

                // Get bus object confirm will have one
                BusServices busObj = null;
                for (BusServices b : busObjArr) {
                    if (b.getServiceNo().equalsIgnoreCase(item.getServiceNo()) && b.getStopID().equalsIgnoreCase(mainArr.getBusStopCode())) {
                        busObj = b;
                        break;
                    }
                }

                if (busObj == null) {
                    LogHelper.e("GET-FAV-BUS-SERVICE", "Cannot find bus object. Something went wrong!");
                    continue;
                }

                BusStatus nextBus = new BusStatus();
                nextBus.setEstimatedArrival(item.getNextBus().getEstimatedArrival());
                nextBus.setIsWheelChairAccessible(item.getNextBus().getFeature());
                nextBus.setLoad(item.getNextBus().getLoad());

                BusStatus subsequentBus = new BusStatus();
                subsequentBus.setEstimatedArrival(item.getNextBus2().getEstimatedArrival());
                subsequentBus.setIsWheelChairAccessible(item.getNextBus2().getFeature());
                subsequentBus.setLoad(item.getNextBus2().getLoad());

                //New API changes (12 November 2016 and 30 July 2017)

                //First add the required stuff to the other 2
                nextBus.setVisitNumber(item.getNextBus().getVisitNumberD());
                nextBus.setLatitude(item.getNextBus().getLatitudeD());
                nextBus.setLongitude(item.getNextBus().getLongitudeD());
                nextBus.setBusType(item.getNextBus().getType());
                nextBus.setTerminatingID(item.getNextBus().getDestinationCode());
                nextBus.setOriginatingID(item.getNextBus().getOriginCode());

                subsequentBus.setVisitNumber(item.getNextBus2().getVisitNumberD());
                subsequentBus.setLatitude(item.getNextBus2().getLatitudeD());
                subsequentBus.setLongitude(item.getNextBus2().getLongitudeD());
                subsequentBus.setBusType(item.getNextBus2().getType());
                subsequentBus.setTerminatingID(item.getNextBus2().getDestinationCode());
                subsequentBus.setOriginatingID(item.getNextBus2().getOriginCode());


                BusStatus subsequent2Bus = new BusStatus();
                subsequent2Bus.setEstimatedArrival(item.getNextBus3().getEstimatedArrival());
                subsequent2Bus.setIsWheelChairAccessible(item.getNextBus3().getFeature());
                subsequent2Bus.setLoad(item.getNextBus3().getLoad());
                subsequent2Bus.setVisitNumber(item.getNextBus3().getVisitNumberD());
                subsequent2Bus.setLatitude(item.getNextBus3().getLatitudeD());
                subsequent2Bus.setLongitude(item.getNextBus3().getLongitudeD());
                subsequent2Bus.setBusType(item.getNextBus3().getType());
                subsequent2Bus.setTerminatingID(item.getNextBus3().getDestinationCode());
                subsequent2Bus.setOriginatingID(item.getNextBus3().getOriginCode());

                busObj.setSubsequentBus(subsequent2Bus);

                // End of New API Changes

                busObj.setCurrentBus(nextBus);
                busObj.setNextBus(subsequentBus);
                busObj.setTime(System.currentTimeMillis());
                busObj.setSvcStatus(checkServiceOperational(nextBus, subsequentBus, subsequent2Bus));
                busObj.setObtainedNextData(true);

                //Go through arrayList and update the current one
                for (int i = 0; i < StaticVariables.INSTANCE.getFavouritesList().size(); i++) {
                    BusServices ob = StaticVariables.INSTANCE.getFavouritesList().get(i);
                    if (ob.getServiceNo().equals(busObj.getServiceNo()) && ob.getStopID().equals(busObj.getStopID())) {
                        //Update
                        StaticVariables.INSTANCE.getFavouritesList().set(i, busObj);
                        adapter.updateAdapter(StaticVariables.INSTANCE.getFavouritesList(), mainArr.getCurrentTime());
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }
            }

        }
    }

    private boolean checkServiceOperational(BusStatus one, BusStatus two, BusStatus three) {
        return !(one.getEstimatedArrival() == null && two.getEstimatedArrival() == null && three.getEstimatedArrival() == null)
                && !(Objects.requireNonNull(one.getEstimatedArrival()).isEmpty() && Objects.requireNonNull(two.getEstimatedArrival()).isEmpty()
                && Objects.requireNonNull(three.getEstimatedArrival()).isEmpty());
    }
}

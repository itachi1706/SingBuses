package com.itachi1706.busarrivalsg.Services;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.itachi1706.busarrivalsg.AsyncTasks.Services.GetFirstFavouriteData;
import com.itachi1706.busarrivalsg.AsyncTasks.Services.GetRemainingFavouriteData;
import com.itachi1706.busarrivalsg.BuildConfig;
import com.itachi1706.busarrivalsg.Objects.BusServices;
import com.itachi1706.busarrivalsg.Util.PebbleEnum;
import com.itachi1706.busarrivalsg.Util.StaticVariables;

import io.fabric.sdk.android.Fabric;

import static com.itachi1706.busarrivalsg.Util.StaticVariables.parseEstimateArrival;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class PebbleCommunications extends Service {

    PebbleKit.PebbleDataReceiver mReceiver;
    PebbleKit.PebbleNackReceiver mNack;
    PebbleKit.PebbleAckReceiver mAck;

    private Looper serviceLooper;
    private ServiceHandler mServiceHandler;

    SharedPreferences sp;

    public PebbleCommunications() {}


    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        super.onStartCommand(intent, flags, startId);
        Fabric fabric = new Fabric.Builder(this).kits(new Crashlytics()).debuggable(BuildConfig.DEBUG).build();
        if (!BuildConfig.DEBUG) Fabric.with(fabric);
        Log.i("Pebble Comm", "SYSTEM: Started Pebble Communications Service");
        Log.i("Pebble Comm", "UUID: " + StaticVariables.PEBBLE_APP_UUID.toString());

        unregisterPebbleReceivers();
        Log.i("Pebble Comm", "Unregistered. Now reregistering receivers");


        mNack = new PebbleKit.PebbleNackReceiver(StaticVariables.PEBBLE_APP_UUID) {
            @Override
            public void receiveNack(Context context, int transactionId) {
                Log.e("Pebble Comm", "Message failed to send to Pebble");
                switch (transactionId){
                    case 1:
                        if (StaticVariables.dict1 != null) PebbleKit.sendDataToPebbleWithTransactionId(getApplicationContext(), StaticVariables.PEBBLE_APP_UUID, StaticVariables.dict1, 1);
                        break;
                    case 2:
                        if (StaticVariables.dict2 != null) PebbleKit.sendDataToPebbleWithTransactionId(getApplicationContext(), StaticVariables.PEBBLE_APP_UUID, StaticVariables.dict2, 2);
                        break;
                    case 3:
                        if (StaticVariables.dict3 != null) PebbleKit.sendDataToPebbleWithTransactionId(getApplicationContext(), StaticVariables.PEBBLE_APP_UUID, StaticVariables.dict3, 3);
                        break;
                    case 4:
                        if (StaticVariables.dict4 != null) PebbleKit.sendDataToPebbleWithTransactionId(getApplicationContext(), StaticVariables.PEBBLE_APP_UUID, StaticVariables.dict4, 4);
                        break;
                }
            }
        };
        PebbleKit.registerReceivedNackHandler(getApplicationContext(), mNack);
        Log.i("Pebble Comm", "Registered Nack Reciver");

        mAck = new PebbleKit.PebbleAckReceiver(StaticVariables.PEBBLE_APP_UUID) {
            @Override
            public void receiveAck(Context context, int transactionId) {
                Log.i("Pebble Comm", "Sent message successfully to Pebble");

                //After sending is successfully, null the dictionary
                switch (transactionId){
                    case 1: //send 2
                        StaticVariables.dict1 = null;
                        if (StaticVariables.dict2 != null) PebbleKit.sendDataToPebbleWithTransactionId(getApplicationContext(), StaticVariables.PEBBLE_APP_UUID, StaticVariables.dict2, 2);
                        break;
                    case 2: //send 3
                        StaticVariables.dict2 = null;
                        if (StaticVariables.dict3 != null) PebbleKit.sendDataToPebbleWithTransactionId(getApplicationContext(), StaticVariables.PEBBLE_APP_UUID, StaticVariables.dict3, 3);
                        break;
                    case 3: //send 4
                        StaticVariables.dict3 = null;
                        if (StaticVariables.dict4 != null) PebbleKit.sendDataToPebbleWithTransactionId(getApplicationContext(), StaticVariables.PEBBLE_APP_UUID, StaticVariables.dict4, 4);
                        break;
                    case 4: StaticVariables.dict4 = null; break;
                }
            }
        };
        PebbleKit.registerReceivedAckHandler(getApplicationContext(), mAck);
        Log.i("Pebble Comm", "Registered Ack Receiver");

        //Handle Data Receiver
        mReceiver = new PebbleKit.PebbleDataReceiver(StaticVariables.PEBBLE_APP_UUID) {
            @Override
            public void receiveData(Context context, int i, PebbleDictionary pebbleDictionary) {
                Log.i("Pebble Comm", "Received Message from Pebble");
                PebbleKit.sendAckToPebble(getApplicationContext(), i);
                Log.i("Pebble Comm", "Sent ACK to Pebble");

                //Handle stuff
                if (pebbleDictionary.contains(PebbleEnum.KEY_BUTTON_EVENT)){
                    Message msg = mServiceHandler.obtainMessage();
                    msg.arg1 = pebbleDictionary.getUnsignedIntegerAsLong(PebbleEnum.KEY_BUTTON_EVENT).intValue();
                    if (pebbleDictionary.contains(PebbleEnum.MESSAGE_CURRENT_FAV)){
                        msg.arg2 = pebbleDictionary.getInteger(PebbleEnum.MESSAGE_CURRENT_FAV).intValue();
                    } else {
                        msg.arg2 = 0;
                    }
                    mServiceHandler.sendMessage(msg);
                }
            }
        };
        PebbleKit.registerReceivedDataHandler(getApplicationContext(), mReceiver);
        Log.i("Pebble Comm", "Register Data Receiver");

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        return START_STICKY;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.i("Pebble Comm", "SYSTEM: Created Service");

        /*
        Starts the thread handling service in another thread to not block the main UI thread.
        Also, it has background priority so CPU intensive work will not disrupt the main UI
         */
        HandlerThread thread = new HandlerThread("PebbleCommunications", Thread.MIN_PRIORITY);
        thread.start();

        serviceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(serviceLooper);

        // Obtain the FirebaseAnalytics instance.
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Launched Pebble Service");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "serviceLaunch");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.i("Pebble Comm", "SYSTEM: Killed Service gracefully");

        unregisterPebbleReceivers();

        // Obtain the FirebaseAnalytics instance.
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Killed Pebble Service");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "serviceStopped");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

    }

    private void unregisterPebbleReceivers(){

            if (mReceiver != null) {
                try {
                    unregisterReceiver(mReceiver);
                } catch (IllegalArgumentException e) {
                    Log.e("PebbleComm", "Already Unregistered. Ignoring");
                }
            }
            if (mAck != null) {
                try {
                    unregisterReceiver(mAck);
                } catch (IllegalArgumentException e) {
                    Log.e("PebbleComm", "Already Unregistered. Ignoring");
                }
            }
            if (mNack != null) {
                try {
                    unregisterReceiver(mNack);
                } catch (IllegalArgumentException e) {
                    Log.e("PebbleComm", "Already Unregistered. Ignoring");
                }
            }

            mReceiver = null;
            mAck = null;
            mNack = null;
    }

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    private class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        // Note: Paring of time is supposed to have an option to use server time, due to this being on a deprecated platform
        // (Pebble), there is no point in investing in effort to use server time here so I'm just not gonna bother :P

        @Override
        public void handleMessage(Message msg) {
            //Do stuff here

            int buttonPress = msg.arg1;
            int currentPage = msg.arg2;

            switch (buttonPress) {
                case PebbleEnum.BUTTON_PREVIOUS:
                    Log.d("Pebble Comm", "Going Previous!");
                    prevHandler(currentPage);
                    break;
                case PebbleEnum.BUTTON_NEXT:
                    Log.d("Pebble Comm", "Going Next!");
                    nextHandler(currentPage);
                    break;
                case PebbleEnum.BUTTON_REFRESH:
                    Log.d("Pebble Comm", "Going Refresh!");
                    refreshHandler();
                    break;
            }
        }

        private void nextHandler(int page) {
            if (page != StaticVariables.favouritesList.size()) {
                //Get Next
                BusServices obj = StaticVariables.favouritesList.get(page);

                if (!obj.isObtainedNextData()) return;

                String currentEst;
                String nextEst;
                if (obj.getNextBus().getEstimatedArrival() == null) {
                    nextEst = "-";
                } else {
                    long estNxt = parseEstimateArrival(obj.getNextBus().getEstimatedArrival(), false, null);
                    if (estNxt <= 0)
                        nextEst = "Arr";
                    else if (estNxt == 1)
                        nextEst = estNxt + " min";
                    else
                        nextEst = estNxt + " mins";
                }
                if (obj.getCurrentBus().getEstimatedArrival() == null) {
                    currentEst = "-";
                } else {
                    long estCur = parseEstimateArrival(obj.getCurrentBus().getEstimatedArrival(), false, null);
                    if (estCur <= 0)
                        currentEst = "Arr";
                    else if (estCur == 1)
                        currentEst = estCur + " min";
                    else
                        currentEst = estCur + " mins";
                }
                //Push to pebble
                PebbleDictionary dict1 = new PebbleDictionary();
                PebbleDictionary dict2 = new PebbleDictionary();
                PebbleDictionary dict3 = new PebbleDictionary();
                dict1.addInt16(PebbleEnum.MESSAGE_DATA_EVENT, (short) 1);
                dict2.addInt16(PebbleEnum.MESSAGE_DATA_EVENT, (short) 2);
                dict3.addInt16(PebbleEnum.MESSAGE_DATA_EVENT, (short) 3);
                if (obj.getStopName() == null)
                    dict1.addString(PebbleEnum.MESSAGE_ROAD_NAME, "Unknown Stop");
                else
                    dict1.addString(PebbleEnum.MESSAGE_ROAD_NAME, obj.getStopName().trim());
                dict2.addString(PebbleEnum.MESSAGE_BUS_SERVICE, obj.getServiceNo().trim());
                dict2.addString(PebbleEnum.MESSAGE_ROAD_CODE, obj.getStopID().trim());
                dict2.addInt16(PebbleEnum.MESSAGE_MAX_FAV, (short) StaticVariables.favouritesList.size());
                dict2.addInt16(PebbleEnum.MESSAGE_CURRENT_FAV, (short) (page + 1));
                dict3.addString(PebbleEnum.ESTIMATE_ARR_CURRENT_DATA, currentEst.trim());
                dict3.addInt16(PebbleEnum.ESTIMATE_LOAD_CURRENT_DATA, (short) obj.getCurrentBus().getLoad());
                dict3.addString(PebbleEnum.ESTIMATE_ARR_NEXT_DATA, nextEst.trim());
                dict3.addInt16(PebbleEnum.ESTIMATE_LOAD_NEXT_DATA, (short) obj.getNextBus().getLoad());

                //Send WAB status
                dict2.addInt8(PebbleEnum.MESSAGE_WAB_CURRENT, StaticVariables.parseWABStatusToPebble(obj.getCurrentBus().isWheelChairAccessible()));
                dict2.addInt8(PebbleEnum.MESSAGE_WAB_NEXT, StaticVariables.parseWABStatusToPebble(obj.getNextBus().isWheelChairAccessible()));

                PebbleKit.sendDataToPebbleWithTransactionId(getApplicationContext(), StaticVariables.PEBBLE_APP_UUID, dict1, 1);
                StaticVariables.dict1 = dict1;
                StaticVariables.dict2 = dict2;
                StaticVariables.dict3 = dict3;
            }
        }

        private void prevHandler(int page) {
            if (page > 1) {
                //Get Previous
                BusServices obj = StaticVariables.favouritesList.get(page - 2);

                if (!obj.isObtainedNextData()) return;

                String currentEst;
                String nextEst;
                if (obj.getNextBus().getEstimatedArrival() == null) {
                    nextEst = "-";
                } else {
                    long estNxt = parseEstimateArrival(obj.getNextBus().getEstimatedArrival(), false, null);
                    if (estNxt <= 0)
                        nextEst = "Arr";
                    else if (estNxt == 1)
                        nextEst = estNxt + " min";
                    else
                        nextEst = estNxt + " mins";
                }
                if (obj.getCurrentBus().getEstimatedArrival() == null) {
                    currentEst = "-";
                } else {
                    long estCur = parseEstimateArrival(obj.getCurrentBus().getEstimatedArrival(), false, null);
                    if (estCur <= 0)
                        currentEst = "Arr";
                    else if (estCur == 1)
                        currentEst = estCur + " min";
                    else
                        currentEst = estCur + " mins";
                }

                //Push to pebble
                PebbleDictionary dict1 = new PebbleDictionary();
                PebbleDictionary dict2 = new PebbleDictionary();
                PebbleDictionary dict3 = new PebbleDictionary();
                dict1.addInt16(PebbleEnum.MESSAGE_DATA_EVENT, (short) 1);
                dict2.addInt16(PebbleEnum.MESSAGE_DATA_EVENT, (short) 2);
                dict3.addInt16(PebbleEnum.MESSAGE_DATA_EVENT, (short) 3);
                if (obj.getStopName() == null)
                    dict1.addString(PebbleEnum.MESSAGE_ROAD_NAME, "Unknown Stop");
                else
                    dict1.addString(PebbleEnum.MESSAGE_ROAD_NAME, obj.getStopName().trim());
                dict2.addString(PebbleEnum.MESSAGE_BUS_SERVICE, obj.getServiceNo().trim());
                dict2.addString(PebbleEnum.MESSAGE_ROAD_CODE, obj.getStopID().trim());
                dict2.addInt16(PebbleEnum.MESSAGE_MAX_FAV, (short) StaticVariables.favouritesList.size());
                dict2.addInt16(PebbleEnum.MESSAGE_CURRENT_FAV, (short) (page - 1));
                dict3.addString(PebbleEnum.ESTIMATE_ARR_CURRENT_DATA, currentEst.trim());
                dict3.addInt16(PebbleEnum.ESTIMATE_LOAD_CURRENT_DATA, (short) obj.getCurrentBus().getLoad());
                dict3.addString(PebbleEnum.ESTIMATE_ARR_NEXT_DATA, nextEst.trim());
                dict3.addInt16(PebbleEnum.ESTIMATE_LOAD_NEXT_DATA, (short) obj.getNextBus().getLoad());

                //Send WAB status
                dict2.addInt8(PebbleEnum.MESSAGE_WAB_CURRENT, StaticVariables.parseWABStatusToPebble(obj.getCurrentBus().isWheelChairAccessible()));
                dict2.addInt8(PebbleEnum.MESSAGE_WAB_NEXT, StaticVariables.parseWABStatusToPebble(obj.getNextBus().isWheelChairAccessible()));

                PebbleKit.sendDataToPebbleWithTransactionId(getApplicationContext(), StaticVariables.PEBBLE_APP_UUID, dict1, 1);
                StaticVariables.dict1 = dict1;
                StaticVariables.dict2 = dict2;
                StaticVariables.dict3 = dict3;
            }
        }

        private void refreshHandler() {
            Log.d("PebbleComm", "Received intent to refresh :D");

            String json = sp.getString("stored", "wot");
            Log.d("FAVOURITES", "Favourites Pref: " + json);

            if (BusStorage.hasFavourites(sp)) {
                //Go ahead with loading and getting data
                Log.d("FAVOURITES", "Has Favourites. Processing");
                StaticVariables.favouritesList = BusStorage.getStoredBuses(sp);

                Log.d("FAVOURITES", "Finished Processing, retrieving estimated arrival data now");
                if (StaticVariables.favouritesList.size() > 0) {
                    //Process first one
                    new GetFirstFavouriteData(getApplicationContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, StaticVariables.favouritesList.get(0));
                }

                boolean first = true;
                for (BusServices s : StaticVariables.favouritesList) {
                    if (first) {
                        first = false;
                        continue;
                    }
                    //Background process the rest of the favourites
                    new GetRemainingFavouriteData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, s);
                }
                Log.d("FAVOURITES", "Finished casting AsyncTasks to retrieve estimated arrival data");
            } else {
                PebbleDictionary dict = new PebbleDictionary();
                dict.addInt32(PebbleEnum.ERROR_NO_DATA, 1);
                StaticVariables.dict1 = dict;
                PebbleKit.sendDataToPebbleWithTransactionId(getApplicationContext(), StaticVariables.PEBBLE_APP_UUID, dict, 1);
            }
        }
    }
}

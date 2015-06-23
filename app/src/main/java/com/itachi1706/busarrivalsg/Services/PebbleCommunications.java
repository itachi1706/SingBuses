package com.itachi1706.busarrivalsg.Services;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.itachi1706.busarrivalsg.StaticVariables;

import java.util.logging.LogRecord;


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

    public PebbleCommunications() {}


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i("Pebble Comm", "SYSTEM: Started Pebble Communications Service");

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

        mNack = new PebbleKit.PebbleNackReceiver(StaticVariables.PEBBLE_APP_UUID) {
            @Override
            public void receiveNack(Context context, int i) {
                Log.e("Pebble Comm", "Message failed to send to Pebble");
            }
        };

        mAck = new PebbleKit.PebbleAckReceiver(StaticVariables.PEBBLE_APP_UUID) {
            @Override
            public void receiveAck(Context context, int i) {
                Log.i("Pebble Comm", "Sent message successfully to Pebble");
            }
        };

        //Handle Data Receiver
        mReceiver = new PebbleKit.PebbleDataReceiver(StaticVariables.PEBBLE_APP_UUID) {
            @Override
            public void receiveData(Context context, int i, PebbleDictionary pebbleDictionary) {
                Log.i("Pebble Comm", "Received Message from Pebble");
                PebbleKit.sendAckToPebble(getApplicationContext(), i);
                Log.i("Pebble Comm", "Sent ACK to Pebble");

                //Handle stuff

            }
        };
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.i("Pebble Comm", "SYSTEM: Killed Service gracefully");
    }

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    private class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg){
            //Do stuff here

        }
    }
}

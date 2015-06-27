package com.itachi1706.busarrivalsg;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.itachi1706.busarrivalsg.Services.PebbleCommunications;

public class CompleteBootReceiver extends BroadcastReceiver {
    public CompleteBootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            Intent startPebbleService = new Intent(context, PebbleCommunications.class);
            if (sp.getBoolean("pebbleSvc", true)){
                context.startService(startPebbleService);
            } else {
                context.stopService(startPebbleService);
            }
        }
    }
}

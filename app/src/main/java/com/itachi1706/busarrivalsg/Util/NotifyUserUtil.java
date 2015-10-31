package com.itachi1706.busarrivalsg.Util;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

/**
 * Created by Kenneth on 30/5/2015
 * for HypixelStatistics in package com.itachi1706.hypixelstatistics.util
 */
public class NotifyUserUtil {

    public static void showShortDismissSnackbar(View currentLayout, String message){
        Snackbar.make(currentLayout, message, Snackbar.LENGTH_SHORT)
                .setAction("DISMISS", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
    }

    public static void createShortToast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}

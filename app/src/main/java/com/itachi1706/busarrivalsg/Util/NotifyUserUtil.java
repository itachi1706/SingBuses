package com.itachi1706.busarrivalsg.Util;

import android.content.Context;
import com.google.android.material.snackbar.Snackbar;
import android.view.View;
import android.widget.Toast;

import com.itachi1706.busarrivalsg.R;

/**
 * Created by Kenneth on 30/5/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.util
 */
public class NotifyUserUtil {

    public static void showShortDismissSnackbar(View currentLayout, String message){
        Snackbar.make(currentLayout, message, Snackbar.LENGTH_SHORT)
                .setAction(R.string.snackbar_action_dismiss, v -> {

                }).show();
    }

    public static void createShortToast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}

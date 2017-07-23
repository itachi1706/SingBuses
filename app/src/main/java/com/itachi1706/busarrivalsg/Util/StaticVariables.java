package com.itachi1706.busarrivalsg.Util;

import android.app.AlertDialog;
import android.util.Log;

import com.getpebble.android.kit.util.PebbleDictionary;
import com.itachi1706.busarrivalsg.Objects.BusServices;
import com.itachi1706.busarrivalsg.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kenneth on 20/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg
 */
public class StaticVariables {

    public static int HTTP_QUERY_TIMEOUT = 15000; //15 seconds timeout
    public static final String BASE_SERVER_URL = "http://api.itachi1706.com/api/appupdatechecker.php?action=androidretrievedata&packagename=";

    public final static UUID PEBBLE_APP_UUID = UUID.fromString("11198668-4e27-4e94-b51c-a27a1ea5cd82");

    public static final int CUR = 0, NEXT = 1, SUB = 2;

    //For Pebble Comm
    public static PebbleDictionary dict1 = null;
    public static PebbleDictionary dict2 = null;
    public static PebbleDictionary dict3 = null;
    public static PebbleDictionary dict4 = null;

    public static ArrayList<BusServices> favouritesList = new ArrayList<>();

    public static boolean checkIfYouGotJsonString(String jsonString){
        return !jsonString.startsWith("<!DOCTYPE html>");
    }

    private static Calendar splitLTADate(String dateString){
        Log.d("SPLIT", "Date String to parse: " + dateString);
        String[] firstSplit = dateString.split("T");
        String date = firstSplit[0];
        String time = firstSplit[1];
        String[] timeSplit = time.split("\\+");
        String trueTime = timeSplit[0];

        String[] dateSplit = date.split("\\-");
        int year = Integer.parseInt(dateSplit[0]);
        int month = Integer.parseInt(dateSplit[1]) - 1;
        int dates = Integer.parseInt(dateSplit[2]);

        String[] trueTimeSplit = trueTime.split(":");
        int hr = Integer.parseInt(trueTimeSplit[0]);
        int min = Integer.parseInt(trueTimeSplit[1]);
        int sec = Integer.parseInt(trueTimeSplit[2]);

        Calendar tmp = new GregorianCalendar(year, month, dates, hr, min, sec);
        //Cause Server gives GMT, we need convert to SST
        tmp.add(Calendar.HOUR, 8);
        //tmp.setTimeZone(new SimpleTimeZone(8000, "SST"));
        return tmp;
    }

    public static long parseLTAEstimateArrival(String arrivalString){
        Log.d("DATE", "Current Time Millis: " + System.currentTimeMillis());
        //GregorianCalendar currentDate = new GregorianCalendar(new SimpleTimeZone(8000, "SST"));
        //currentDate.setTimeInMillis(networkTime[0]);
        //currentDate.setTimeInMillis(System.currentTimeMillis());
        if (arrivalString.equalsIgnoreCase("")) return -9999;
        Calendar currentDate = Calendar.getInstance();

        Calendar arrivalDate = StaticVariables.splitLTADate(arrivalString);

        Log.d("COMPARE","Current: " + currentDate.toString() );
        Log.d("COMPARE", "Arrival: " + arrivalDate.toString());
        long difference = arrivalDate.getTimeInMillis() - currentDate.getTimeInMillis();
        return TimeUnit.MILLISECONDS.toMinutes(difference);
    }

    public static byte parseWABStatusToPebble(boolean status){
        if (status) return Byte.valueOf("1");
        return Byte.valueOf("0");
    }

    public static boolean checkBusLocationValid(double lat, double lng) {
        return !(lng == -1000 || lat == -1000) && !(lng == -11 && lat == -11) && !(lat == 0 && lng == 0);

    }

    // HANDLER MESSAGES
    public static final int BUS_SERVICE_JSON_RETRIEVED = 101;
}

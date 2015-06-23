package com.itachi1706.busarrivalsg;

import com.getpebble.android.kit.util.PebbleDictionary;
import com.itachi1706.busarrivalsg.Objects.BusServices;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Kenneth on 20/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg
 */
public class StaticVariables {

    public static int HTTP_QUERY_TIMEOUT = 15000; //15 seconds timeout

    public final static UUID PEBBLE_APP_UUID = UUID.fromString("11198668-4e27-4e94-b51c-a27a1ea5cd82");

    //For Pebble Comm
    public static PebbleDictionary dict1 = null;
    public static PebbleDictionary dict2 = null;
    public static PebbleDictionary dict3 = null;
    public static PebbleDictionary dict4 = null;
    public static int extraSend = -1;

    public static boolean init1TaskFinished = false;

    public static ArrayList<BusServices> favouritesList = new ArrayList<>();

    public static boolean checkIfYouGotJsonString(String jsonString){
        return !jsonString.startsWith("<!DOCTYPE html>");
    }
}

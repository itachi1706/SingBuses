package com.itachi1706.busarrivalsg;

/**
 * Created by Kenneth on 20/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg
 */
public class StaticVariables {

    public static int HTTP_QUERY_TIMEOUT = 15000; //15 seconds timeout

    public static boolean init1TaskFinished = false;

    public static boolean checkIfYouGotJsonString(String jsonString){
        return !jsonString.startsWith("<!DOCTYPE html>");
    }
}

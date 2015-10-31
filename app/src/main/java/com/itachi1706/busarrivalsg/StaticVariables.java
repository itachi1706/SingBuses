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

    public static String getChangelogStringFromArrayList(ArrayList<String> changelog){
        /**
         * Legend of Stuff
         * 1st Line - Current Version Code Check
         * 2nd Line - Current Version Number
         * 3rd Line - Link to New Version
         * # - Changelog Version Number (Bold)
         * * - Points
         * @ - Break Line
         */
        StringBuilder changelogBuilder = new StringBuilder();
        changelogBuilder.append("Latest Version: ").append(changelog.get(1)).append("-b").append(changelog.get(0)).append("<br/><br/>");
        for (String line : changelog){
            if (line.startsWith("#"))
                changelogBuilder.append("<b>").append(line.replace('#', ' ')).append("</b><br />");
            else if (line.startsWith("*"))
                changelogBuilder.append(" - ").append(line.replace('*', ' ')).append("<br />");
            else if (line.startsWith("@"))
                changelogBuilder.append("<br />");
        }
        return changelogBuilder.toString();
    }
}

package com.itachi1706.busarrivalsg.Interface;

import com.itachi1706.busarrivalsg.gsonObjects.sgLTA.BusArrivalArrayObject;
import com.itachi1706.busarrivalsg.objects.BusServices;

/**
 * Created by Kenneth on 31/10/2015.
 * for SingBuses in package com.itachi1706.busarrivalsg
 */
public interface IHandleStuff {
    void favouriteOrUnfavourite(BusServices fav, BusArrivalArrayObject item);
}

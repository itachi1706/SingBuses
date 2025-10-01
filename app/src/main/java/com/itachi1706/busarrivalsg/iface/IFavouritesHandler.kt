package com.itachi1706.busarrivalsg.iface

import com.itachi1706.busarrivalsg.objects.BusServices
import com.itachi1706.busarrivalsg.objects.json.ltasg.BusArrivalArrayObject

fun interface IFavouritesHandler {
    fun favouriteOrUnfavourite(fav: BusServices, item: BusArrivalArrayObject)
}
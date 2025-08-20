package com.itachi1706.busarrivalsg.iface

import com.itachi1706.busarrivalsg.objects.BusServices
import com.itachi1706.busarrivalsg.objects.gson.ltasg.BusArrivalArrayObject

fun interface IHandle {
    fun favouriteOrUnfavourite(fav: BusServices, item: BusArrivalArrayObject)
}
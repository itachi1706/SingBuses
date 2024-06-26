package com.itachi1706.busarrivalsg.gsonObjects.offline

/**
 * Created by Kenneth on 17/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.gsonObjects.offline
 */
data class BusArrayJSON(val storage: Array<BusJSON>? = null) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BusArrayJSON

        if (storage != null) {
            if (other.storage == null) return false
            if (!storage.contentEquals(other.storage)) return false
        } else if (other.storage != null) return false

        return true
    }

    override fun hashCode(): Int {
        return storage?.contentHashCode() ?: 0
    }
}

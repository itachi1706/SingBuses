package com.itachi1706.busarrivalsg.objects.json.ltasg

data class BusStopJSONArray(val value: Array<BusStopJSON>? = null) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BusStopJSONArray

        if (value != null) {
            if (other.value == null) return false
            if (!value.contentEquals(other.value)) return false
        } else if (other.value != null) return false

        return true
    }

    override fun hashCode(): Int {
        return value?.contentHashCode() ?: 0
    }
}

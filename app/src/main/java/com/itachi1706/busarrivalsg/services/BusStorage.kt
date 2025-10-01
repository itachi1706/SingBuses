package com.itachi1706.busarrivalsg.services

import android.content.SharedPreferences
import androidx.core.content.edit
import com.itachi1706.busarrivalsg.objects.BusServices
import com.itachi1706.busarrivalsg.objects.json.offline.BusArrayJSON
import com.itachi1706.helperlib.helpers.LogHelper
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class BusStorage(val prefs: SharedPreferences) {

    fun addNewBus(bus: BusServices) {
        val arr = getExistingJsonString()
        val obj = JSONObject()
        val main = JSONObject()

        try {
            obj.put("service", bus.serviceNo)
            obj.put("operator", bus.operator)
            obj.put("stop", bus.stopID)
            obj.put("stopName", bus.stopName)
            arr.put(obj)
            main.put("storage", arr)
            prefs.edit { putString(PREF_STORED_NAME, main.toString()) }
        } catch (e: JSONException) {
            LogHelper.e(TAG, "Error adding new bus to storage")
            LogHelper.e(TAG, "Error message: ${e.localizedMessage}")
        }
    }

    fun getStoredBuses(): List<BusServices> {
        val json = prefs.getString(PREF_STORED_NAME, null)
        if (json.isNullOrEmpty()) {
            return emptyList()
        }

        val services = ArrayList<BusServices>()

        val jsonConfig = Json { ignoreUnknownKeys = true }
        val busArray = jsonConfig.decodeFromString<BusArrayJSON>(json)
        if (busArray == null || busArray.storage.isNullOrEmpty()) {
            return emptyList()
        }
        for (bus in busArray.storage) {
            val service = BusServices(
                serviceNo = bus.service ?: "",
                operator = bus.operator ?: "",
                stopID = bus.stop ?: "",
                stopName = bus.stopName ?: "",
                isObtainedNextData = false
            )
            services.add(service)
        }

        return services
    }

    fun updateBusJson(newServices: List<BusServices>) {
        prefs.edit{ remove(PREF_STORED_NAME) }
        if (newServices.isNotEmpty()) {
            newServices.forEach { addNewBus(it) }
        }
    }

    fun hasFavourites(): Boolean {
        val check = prefs.getString(PREF_STORED_NAME, "wot")
        return check != "wot"
    }

    private fun getExistingJsonString(): JSONArray {
        val json = prefs.getString(PREF_STORED_NAME, null)
        if (json.isNullOrEmpty()) {
            return JSONArray()
        }

        return try {
            val obj = JSONObject(json)
            obj.getJSONArray("storage")
        } catch (e: Exception) {
            LogHelper.e(TAG, "Error getting existing JSON string")
            LogHelper.e(TAG, "Error message: ${e.localizedMessage}")
            JSONArray()
        }
    }

    companion object {
        const val TAG = "BusStorage"
        private const val PREF_STORED_NAME = "stored"
    }
}
package com.itachi1706.busarrivalsg.util

import android.content.Context
import androidx.annotation.Nullable
import com.google.gson.Gson
import com.itachi1706.busarrivalsg.objects.gson.ntubuses.NTUBus
import com.itachi1706.helperlib.helpers.LogHelper
import java.io.*
import java.util.*

/**
 * Created by Kenneth on 6/12/2018.
 * for com.itachi1706.busarrivalsg.Util in SingBuses
 */
class NTURouteCacher(private val mContext: Context) {

    private val directory: File
        get() = File(mContext.cacheDir, "ntu")

    init {
        init()
    }

    private fun init() {
        val directory = directory
        if (!directory.exists() || directory.exists() && directory.isFile && directory.delete()) directory.mkdirs()
    }

    fun clearAllCachedFile() {
        val routes = intArrayOf(44478, 44479, 44480, 44481)

        for (r in routes) {
            val f = getFileObject(r.toString())
            if (f.exists()) f.delete()
            LogHelper.i(TAG, "Cleared cache file for Route " + r)
        }
    }

    fun hasCachedFile(routeCode: String): Boolean {
        // Check for cached file as well as if it has expired or not
        val f = getFileObject(routeCode)
        if (!f.exists()) return false

        val lastModified = Date(f.lastModified())
        LogHelper.i(TAG, "Found cache for Route " + routeCode + ", last updated on " + lastModified.toString())

        // Check if greater than cache (1 week)
        val currentTime = System.currentTimeMillis()
        val fileTime = lastModified.time
        val diff = currentTime - fileTime
        if (diff > 7 * 24 * 60 * 60 * 1000) {
            f.delete()
            return false
        }
        return true
    }

    private fun getFileObject(routeCode: String): File {
        return File(directory.absolutePath + "/ntu-route-" + routeCode + ".json")
    }

    @Nullable
    private fun getCachedFile(routeCode: String): File? {
        return if (hasCachedFile(routeCode)) {
            getFileObject(routeCode)
        } else null
    }

    fun getCachedRoute(routeCode: String): String? {
        val f = getCachedFile(routeCode) ?: return null

        // Read text file and return it
        val sb = StringBuilder()
        try {
            val fis = FileInputStream(f)
            val br = BufferedReader(InputStreamReader(fis))
            for (line in br.readLine()) {
                sb.append(line)
            }
            br.close()
            fis.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            LogHelper.e(TAG, "Cannot parse file, assuming corrupted")
            return null
        } catch (e: IOException) {
            e.printStackTrace()
            LogHelper.e(TAG, "Cannot parse file, assuming corrupted")
            return null
        }

        LogHelper.i(TAG, "Loaded $routeCode from cache")
        return sb.toString()
    }

    fun writeCachedRoute(routeCode: String, route: NTUBus.MapRouting?): Boolean {
        if (route == null) return false
        val routeString = getStringFromRoute(route)
        return writeCachedRoute(routeCode, routeString)
    }

    private fun writeCachedRoute(routeCode: String, routeData: String): Boolean {
        val f = getFileObject(routeCode)
        if (f.exists() && !f.delete()) {
            LogHelper.e(TAG, "Unable to remove old cache. Not proceeding")
            return false
        }

        try {
            val fos = FileOutputStream(f)
            fos.write(routeData.toByteArray())
            fos.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            LogHelper.e(TAG, "Cannot write cache, assuming cache write fail")
            return false
        } catch (e: IOException) {
            e.printStackTrace()
            LogHelper.e(TAG, "IO Exception writing cache, assuming write fail")
            return false
        }

        LogHelper.i(TAG, "Wrote $routeCode to cache")
        return true
    }

    fun getRouteFromString(routeString: String): NTUBus.MapRouting {
        val gson = Gson()
        return gson.fromJson(routeString, NTUBus.MapRouting::class.java)
    }

    private fun getStringFromRoute(routes: NTUBus.MapRouting): String {
        val gson = Gson()
        return gson.toJson(routes)
    }

    fun getRouteCode(routeId: Int): String {
        when (routeId) {
            44478 -> return "red"
            44479 -> return "blue"
            44480 -> return "green"
            44481 -> return "brown"
            else -> return "red"
        }
    }

    companion object {
        private val TAG = "NTU-ROUTE-CACHE"
    }
}

package com.itachi1706.busarrivalsg.Util

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.itachi1706.busarrivalsg.objects.CommonEnums



/**
 * Created by Kenneth on 28/8/2018.
 * for com.itachi1706.busarrivalsg.Util in SingBuses
 */
object BusesUtil {

    fun getLoad(load: String?): Int {
        if (load == null) return CommonEnums.UNKNOWN
        return when (load) {
            "SEA" -> CommonEnums.BUS_SEATS_AVAIL
            "SDA" -> CommonEnums.BUS_STANDING_AVAIL
            "LSD" -> CommonEnums.BUS_LIMITED_SEATS
            else -> CommonEnums.UNKNOWN
        }
    }

    fun getType(type: String): Int {
        return when (type) {
            "SD" -> CommonEnums.BUS_SINGLE_DECK
            "DD" -> CommonEnums.BUS_DOUBLE_DECK
            "BD" -> CommonEnums.BUS_BENDY
            else -> CommonEnums.UNKNOWN
        }
    }

    fun getType(type: Int): String {
        return when (type) {
            CommonEnums.BUS_SINGLE_DECK -> "Normal"
            CommonEnums.BUS_DOUBLE_DECK -> "Double"
            CommonEnums.BUS_BENDY -> "Bendy"
            else -> "Unknown"
        }
    }

    fun vectorToBitmapDescriptor(@DrawableRes id: Int, resource: Resources): BitmapDescriptor {
        return vectorToBitmapDescriptor(id, resource, null)
    }

    fun vectorToBitmapDescriptor(@DrawableRes id: Int, resource: Resources, @ColorInt color: Int?): BitmapDescriptor {
        return BitmapDescriptorFactory.fromBitmap(vectorToBitmap(id, resource, color))
    }

    fun vectorToBitmap(@DrawableRes id: Int, resource: Resources, @ColorInt color: Int?): Bitmap {
        val vectorDrawable = ResourcesCompat.getDrawable(resource, id, null)
        val bitmap = Bitmap.createBitmap(vectorDrawable!!.intrinsicWidth,
                vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        if (color != null) {
            vectorDrawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
        vectorDrawable.draw(canvas)
        return bitmap
    }

    fun pxFromDp(dp: Float, resource: Resources): Float {
        return dp * resource.displayMetrics.density
    }
}

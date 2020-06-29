package com.itachi1706.busarrivalsg.util

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.itachi1706.busarrivalsg.R
import com.itachi1706.busarrivalsg.objects.CommonEnums
import com.itachi1706.helperlib.helpers.PrefHelper
import java.util.*


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

    fun vectorToBitmapDescriptor(@DrawableRes id: Int, mContext: Context): BitmapDescriptor {
        return vectorToBitmapDescriptor(id, mContext, null)
    }

    fun vectorToBitmapDescriptor(@DrawableRes id: Int, mContext: Context, @ColorInt color: Int?): BitmapDescriptor {
        return BitmapDescriptorFactory.fromBitmap(vectorToBitmap(id, mContext, color))
    }

    fun vectorToBitmap(@DrawableRes id: Int, mContext: Context, @ColorInt color: Int?): Bitmap {
        val vectorDrawable = AppCompatResources.getDrawable(mContext, id)
        val bitmap = Bitmap.createBitmap(vectorDrawable!!.intrinsicWidth,
                vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        if (color != null)
            vectorDrawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(color, BlendModeCompat.SRC_ATOP)
        vectorDrawable.draw(canvas)
        return bitmap
    }

    fun pxFromDp(dp: Float, resource: Resources): Float {
        return dp * resource.displayMetrics.density
    }

    fun getOperatorColor(context: Context, operator: String) : Int {
        return when (operator.toUpperCase(Locale.getDefault())) {
            OPERATOR_SMRT -> Color.RED
            OPERATOR_SBST -> Color.MAGENTA
            OPERATOR_TTS -> if (PrefHelper.isNightModeEnabled(context)) Color.GREEN else ContextCompat.getColor(context, R.color.dark_green)
            OPERATOR_GAS -> if (PrefHelper.isNightModeEnabled(context)) Color.YELLOW else ContextCompat.getColor(context, R.color.dark_yellow)
            else -> Color.WHITE
        }
    }

    fun applyColorLoad(view: TextView, load: Int) {
        if (view.text.toString().equals("", ignoreCase = true) || view.text.toString().equals("-", ignoreCase = true)) {
            view.setTextColor(Color.GRAY)
            return
        }
        when (load) {
            CommonEnums.BUS_SEATS_AVAIL -> view.setTextColor(if (PrefHelper.isNightModeEnabled(view.context)) Color.GREEN else ContextCompat.getColor(view.context, R.color.dark_green))
            CommonEnums.BUS_STANDING_AVAIL -> view.setTextColor(if (PrefHelper.isNightModeEnabled(view.context)) Color.YELLOW else ContextCompat.getColor(view.context, R.color.dark_yellow))
            CommonEnums.BUS_LIMITED_SEATS -> view.setTextColor(Color.RED)
            else -> view.setTextColor(Color.GRAY)
        }
    }

    private const val OPERATOR_SMRT = "SMRT" // SMRT Buses
    private const val OPERATOR_SBST = "SBST" // SBS Transit
    private const val OPERATOR_TTS = "TTS" // Tower Transit Singapore
    private const val OPERATOR_GAS = "GAS" // Go Ahead Singapore
}

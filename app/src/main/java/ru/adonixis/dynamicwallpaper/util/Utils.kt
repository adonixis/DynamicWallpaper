package ru.adonixis.dynamicwallpaper.util

import android.graphics.BitmapFactory
import java.util.*

object Utils {

    fun minutesToStringTime(totalMinutes: Int): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return String.format(Locale.getDefault(), "%02d", hours) + ":" + String.format(Locale.getDefault(), "%02d", minutes)
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqSide: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqSide || width > reqSide) {
            while (height / inSampleSize >= reqSide &&
                    width / inSampleSize >= reqSide) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun toCamelCase(s: String?): String {
        val parts = s!!.split("_".toRegex()).toTypedArray()
        val camelCaseString = StringBuilder()
        for (part in parts) {
            camelCaseString.append(toProperCase(part))
        }
        return camelCaseString.toString().trim { it <= ' ' }
    }

    fun toProperCase(s: String): String {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase() + " "
    }
}
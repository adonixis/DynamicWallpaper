package ru.adonixis.dynamicwallpaper.util

import android.view.View

interface OnItemClickListener {
    fun onItemClick(view: View, position: Int)
}
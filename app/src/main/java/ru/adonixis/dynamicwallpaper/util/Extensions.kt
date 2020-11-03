package ru.adonixis.dynamicwallpaper.util

import ru.adonixis.dynamicwallpaper.model.Frame

fun MutableList<Frame>.shiftFramesUp() {
    val temp = this.first().filePath
    for (i in 1 until this.size) {
        this[i - 1].filePath = this[i].filePath
    }
    this.last().filePath = temp
}

fun MutableList<Frame>.shiftFramesDown() {
    val temp = this.last().filePath
    for (i in this.size - 1 downTo 1) {
        this[i].filePath = this[i - 1].filePath
    }
    this.first().filePath = temp
}
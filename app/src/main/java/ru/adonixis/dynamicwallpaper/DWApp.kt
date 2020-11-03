package ru.adonixis.dynamicwallpaper

import android.app.Application
import com.huawei.hms.ads.HwAds

class DWApp : Application() {
    override fun onCreate() {
        super.onCreate()
        HwAds.init(this)
    }
}
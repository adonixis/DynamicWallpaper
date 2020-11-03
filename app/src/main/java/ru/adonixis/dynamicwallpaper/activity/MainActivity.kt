package ru.adonixis.dynamicwallpaper.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.BannerAdSize
import com.huawei.hms.ads.banner.BannerView
import ru.adonixis.dynamicwallpaper.R
import ru.adonixis.dynamicwallpaper.adapter.WallpapersAdapter
import ru.adonixis.dynamicwallpaper.util.OnItemClickListener
import java.io.IOException


class MainActivity : AppCompatActivity() {
    companion object {
        const val WALLPAPER_KEY = "wallpaper"
    }

    private lateinit var recyclerWallpapers: RecyclerView
    private lateinit var wallpapersAdapter: WallpapersAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var wallpapers: Array<String>

    private val onWallpaperClickListener = object : OnItemClickListener {
        override fun onItemClick(view: View, position: Int) {
            val wallpaper = wallpapers[position]
            val intent = Intent(this@MainActivity, WallpaperSettingsActivity::class.java)
            intent.putExtra(WALLPAPER_KEY, wallpaper)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try {
            wallpapers = assets.list("dwp")!!
        } catch (e: IOException) {
            e.printStackTrace()
        }

        recyclerWallpapers = findViewById(R.id.recyclerWallpapers)
        recyclerWallpapers.setHasFixedSize(true)
        recyclerWallpapers.setItemViewCacheSize(20)
        layoutManager = GridLayoutManager(this, 2)
        recyclerWallpapers.layoutManager = layoutManager
        wallpapersAdapter = WallpapersAdapter(this, wallpapers, onWallpaperClickListener)
        recyclerWallpapers.adapter = wallpapersAdapter

        val bannerView = findViewById<BannerView>(R.id.hwBannerView)
        bannerView.adId = getString(R.string.hw_ad_id)
        bannerView.bannerAdSize = BannerAdSize.BANNER_SIZE_SMART
        val adParam = AdParam.Builder().build()
        bannerView.loadAd(adParam)
    }
}
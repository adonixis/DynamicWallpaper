package ru.adonixis.dynamicwallpaper.adapter

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.adonixis.dynamicwallpaper.R
import ru.adonixis.dynamicwallpaper.adapter.WallpapersAdapter.WallpaperViewHolder
import ru.adonixis.dynamicwallpaper.util.OnItemClickListener
import ru.adonixis.dynamicwallpaper.util.Utils
import java.io.IOException
import java.util.*

class WallpapersAdapter(
        private val context: Context,
        private val wallpapers: Array<String>,
        private val onWallpaperClickListener: OnItemClickListener
) : RecyclerView.Adapter<WallpaperViewHolder>() {

    inner class WallpaperViewHolder internal constructor(v: View) : RecyclerView.ViewHolder(v) {
        val imageWallpaper: ImageView
        val tvWallpaperName: TextView

        init {
            v.setOnClickListener { onWallpaperClickListener.onItemClick(v, adapterPosition) }
            imageWallpaper = itemView.findViewById(R.id.image_wallpaper)
            tvWallpaperName = itemView.findViewById(R.id.tv_wallpaper_name)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WallpaperViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_wallpaper, parent, false)
        return WallpaperViewHolder(view)
    }

    override fun onBindViewHolder(holder: WallpaperViewHolder, position: Int) {
        val wallpaper = wallpapers[position]
        holder.tvWallpaperName.text = Utils.toCamelCase(wallpaper)

        try {
            val arrayAssets = context.assets.list("dwp/$wallpaper")
            val framesCount = arrayAssets!!.size
            val frameDuration = MINUTES_IN_DAY / framesCount
            val hours = Calendar.getInstance(Locale.getDefault())[Calendar.HOUR_OF_DAY]
            val minutes = Calendar.getInstance(Locale.getDefault())[Calendar.MINUTE]
            val resultMinutes = hours * 60 + minutes
            var i = resultMinutes / frameDuration
            if (i > framesCount - 1) {
                i = framesCount - 1
            }
            Glide.with(context)
                    .load(Uri.parse(String.format(Locale.getDefault(), "file:///android_asset/dwp/$wallpaper/${wallpaper}_%02d.webp", i)))
                    .dontAnimate()
                    .centerCrop()
                    .into(holder.imageWallpaper)
        } catch (e: IOException) {
            Log.e("Wallpaper", "Could not load asset", e)
        }
    }

    override fun getItemCount(): Int {
        return wallpapers.size
    }

    companion object {
        private const val MINUTES_IN_DAY = 1440
    }

}
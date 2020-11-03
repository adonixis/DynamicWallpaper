package ru.adonixis.dynamicwallpaper.activity

import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.adonixis.dynamicwallpaper.R
import ru.adonixis.dynamicwallpaper.adapter.FramesAdapter
import ru.adonixis.dynamicwallpaper.model.Frame
import ru.adonixis.dynamicwallpaper.service.DynamicWallpaperService
import ru.adonixis.dynamicwallpaper.util.OnItemClickListener
import ru.adonixis.dynamicwallpaper.util.Utils
import ru.adonixis.dynamicwallpaper.util.shiftFramesDown
import ru.adonixis.dynamicwallpaper.util.shiftFramesUp
import java.io.IOException
import java.util.*

class WallpaperSettingsActivity : AppCompatActivity() {
    companion object {
        private const val MINUTES_IN_DAY = 1440
        const val FRAMES_KEY = "frames"
    }

    private lateinit var wallpaper: String
    private lateinit var recyclerFrames: RecyclerView
    private lateinit var framesAdapter: FramesAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private var frames: MutableList<Frame> = ArrayList()
    private lateinit var settings: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private val onFrameTimeClickListener = object : OnItemClickListener {
        override fun onItemClick(view: View, position: Int) {
            val frame = frames[position]
            val hours = frame.minutes / 60
            val minutes = frame.minutes % 60
            TimePickerDialog(
                    this@WallpaperSettingsActivity,
                    R.style.TimePickerTheme,
                    OnTimeSetListener { timePicker, hourOfDay, minute ->
                        frame.minutes = hourOfDay * 60 + minute
                        frames.sortWith(Comparator { frameFirst, frameSecond -> frameFirst.minutes - frameSecond.minutes })
                        framesAdapter.notifyDataSetChanged()
                    },
                    hours,
                    minutes,
                    true
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallpaper_settings)
        wallpaper = intent.extras!!.getString(MainActivity.WALLPAPER_KEY, "dwp/mojave")
        title = Utils.toCamelCase(wallpaper)
        settings = PreferenceManager.getDefaultSharedPreferences(this)
        editor = settings.edit()
        val btnSetWallpaper = findViewById<Button>(R.id.btn_set_wallpaper)
        btnSetWallpaper.setOnClickListener {
            val wallpaperManager = WallpaperManager.getInstance(this)
            val wallpaperInfo = wallpaperManager.wallpaperInfo

            DynamicWallpaperService.currentIndex = -1
            val gson = Gson()
            val json = gson.toJson(frames)
            editor.putString(FRAMES_KEY, json)
            editor.putString(MainActivity.WALLPAPER_KEY, wallpaper)
            editor.apply()

            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(this@WallpaperSettingsActivity, DynamicWallpaperService::class.java))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
        val savedWallpaper = settings.getString(MainActivity.WALLPAPER_KEY, "")
        if (savedWallpaper == wallpaper) {
            val frameListType = object : TypeToken<List<Frame?>?>() {}.type
            val gson = Gson()
            val json = settings.getString(FRAMES_KEY, "")
            val savedFrames = gson.fromJson<MutableList<Frame>>(json, frameListType)
            frames = savedFrames
        } else {
            var arrayAssets: Array<String>? = null
            try {
                arrayAssets = assets.list("dwp/$wallpaper")
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val framesCount = arrayAssets!!.size
            val frameDuration = MINUTES_IN_DAY / framesCount
            for (i in 0 until framesCount) {
                frames.add(Frame(frameDuration * i, arrayAssets[i]))
            }
        }
        recyclerFrames = findViewById(R.id.recyclerFrames)
        recyclerFrames.setHasFixedSize(true)
        recyclerFrames.setItemViewCacheSize(20)
        layoutManager = LinearLayoutManager(this)
        recyclerFrames.layoutManager = layoutManager
        framesAdapter = FramesAdapter(this, frames, onFrameTimeClickListener)
        recyclerFrames.adapter = framesAdapter

        val btnUp = findViewById<Button>(R.id.btn_up)
        btnUp.setOnClickListener {
            frames.shiftFramesUp()
            framesAdapter.notifyDataSetChanged()
        }
        val btnDown = findViewById<Button>(R.id.btn_down)
        btnDown.setOnClickListener {
            frames.shiftFramesDown()
            framesAdapter.notifyDataSetChanged()
        }
    }

}
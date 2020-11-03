package ru.adonixis.dynamicwallpaper.service

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.adonixis.dynamicwallpaper.activity.MainActivity
import ru.adonixis.dynamicwallpaper.activity.WallpaperSettingsActivity
import ru.adonixis.dynamicwallpaper.model.Frame
import java.io.IOException
import java.io.InputStream
import java.util.*

class DynamicWallpaperService : WallpaperService() {
    companion object {
        private const val TAG = "DynamicWallpaperService"
        var currentIndex = -1
    }

    private lateinit var settings: SharedPreferences
    private lateinit var wallpaper: String
    private lateinit var frames: List<Frame>
    private var pictureWidth = 0
    private var pictureHeight = 0
    private var nextBitmap: Bitmap? = null
    private var prevBitmap: Bitmap? = null
    //private var currentIndex = -1
    private val nextAlphaPaint = Paint()
    private val prevAlphaPaint = Paint()
    private var nextAlpha = 0
    private var prevAlpha = 255

    override fun onCreateEngine(): Engine {
        super.onCreate()
        settings = PreferenceManager.getDefaultSharedPreferences(this)
        wallpaper = settings.getString(MainActivity.WALLPAPER_KEY, "dwp/mojave") ?: "dwp/mojave"
        val frameListType = object : TypeToken<List<Frame?>?>() {}.type
        val gson = Gson()
        val json = settings.getString(WallpaperSettingsActivity.FRAMES_KEY, "")
        frames = gson.fromJson<List<Frame>>(json, frameListType)

        var inputStream: InputStream? = null
        try {
            inputStream = assets.open("dwp/$wallpaper/${wallpaper}_00.webp")
            val bitmap = BitmapFactory.decodeStream(inputStream)
            pictureWidth = bitmap.width
            pictureHeight = bitmap.height
        } catch (e: IOException) {
            Log.e("Wallpaper", "Could not load asset", e)
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (e: IOException) {
                    Log.e("Wallpaper", "Could not close InputStream", e)
                }
            }
        }

        return DynamicWallpaperEngine()
    }

    private inner class DynamicWallpaperEngine internal constructor() : Engine() {
        private lateinit var holder: SurfaceHolder
        private var visible = false
        private val handler: Handler
        private lateinit var canvas: Canvas
        private var xOffset = 0f
        private var screenCount = 1
        private var x = 0f
        private val drawFrame = Runnable { draw() }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            holder = surfaceHolder
        }

        private fun draw() {
            try {
                canvas = holder.lockCanvas()
                canvas.save()
                val scale: Float
                val screenWidth = canvas.width.toFloat()
                val screenHeight = canvas.height.toFloat()
                scale = if (screenHeight > screenWidth) {
                    screenHeight / pictureHeight
                } else {
                    screenWidth / pictureWidth
                }
                canvas.scale(scale, scale)
                val scaledPictureWidth = pictureWidth * scale
                x = if (screenWidth * screenCount > scaledPictureWidth) {
                    -(xOffset * (scaledPictureWidth - screenWidth) / scale)
                } else {
                    -(scaledPictureWidth / 2 - screenWidth / 2 * screenCount + xOffset * screenWidth * (screenCount - 1)) / scale
                }
                val hours = Calendar.getInstance(Locale.getDefault())[Calendar.HOUR_OF_DAY]
                val minutes = Calendar.getInstance(Locale.getDefault())[Calendar.MINUTE]
                val resultMinutes = hours * 60 + minutes
                var index = 0
                var filePath: String? = ""
                if (frames.first().minutes > resultMinutes || frames.last().minutes < resultMinutes) {
                    index = frames.size - 1
                    filePath = frames[index].filePath
                } else {
                    for (i in 1 until frames.size) {
                        if (frames[i].minutes > resultMinutes) {
                            index = i - 1
                            filePath = frames[index].filePath
                            break
                        }
                    }
                }

                if (currentIndex != index || nextBitmap == null) {
                    currentIndex = index
                    prevAlpha = 255
                    prevAlphaPaint.alpha = prevAlpha
                    nextAlpha = 0
                    nextAlphaPaint.alpha = nextAlpha

                    var inputStream: InputStream? = null
                    try {
                        inputStream = assets.open("dwp/$wallpaper/$filePath")
                        Log.d(TAG, "draw: i = $index")
                        prevBitmap = nextBitmap
                        nextBitmap = BitmapFactory.decodeStream(inputStream)
                    } catch (e: IOException) {
                        Log.e("Wallpaper", "Could not load asset", e)
                    } finally {
                        if (inputStream != null) {
                            try {
                                inputStream.close()
                            } catch (e: IOException) {
                                Log.e("Wallpaper", "Could not close InputStream", e)
                            }
                        }
                    }
                }
                if (prevBitmap == null) {
                    canvas.drawBitmap(nextBitmap!!, x, 0f, null)
                } else {
                    canvas.drawBitmap(nextBitmap!!, x, 0f, null)
                    if (prevAlpha > 0) {
                        prevAlpha--
                        prevAlphaPaint.alpha = prevAlpha
                        canvas.drawBitmap(prevBitmap!!, x, 0f, prevAlphaPaint)
                    }
                }
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
            if (visible) {
                handler.removeCallbacks(drawFrame)
                handler.post(drawFrame)
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) {
                handler.post(drawFrame)
            } else {
                handler.removeCallbacks(drawFrame)
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            handler.removeCallbacks(drawFrame)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            visible = false
            handler.removeCallbacks(drawFrame)
        }

        override fun onOffsetsChanged(xOffset: Float, yOffset: Float, xOffsetStep: Float, yOffsetStep: Float, xPixelOffset: Int, yPixelOffset: Int) {
            super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset)
            this.xOffset = xOffset
            screenCount = if (xOffsetStep <= 0 || xOffsetStep == Float.POSITIVE_INFINITY) {
                1
            } else {
                (1 / xOffsetStep).toInt() + 1
            }
        }

        init {
            handler = Handler()
        }
    }
}
package ru.adonixis.dynamicwallpaper.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.adonixis.dynamicwallpaper.R
import ru.adonixis.dynamicwallpaper.adapter.FramesAdapter.FrameViewHolder
import ru.adonixis.dynamicwallpaper.model.Frame
import ru.adonixis.dynamicwallpaper.util.OnItemClickListener
import ru.adonixis.dynamicwallpaper.util.Utils

class FramesAdapter(private val context: Context, private val frames: List<Frame>?, private val onFrameTimeClickListener: OnItemClickListener) : RecyclerView.Adapter<FrameViewHolder>() {

    inner class FrameViewHolder internal constructor(v: View?) : RecyclerView.ViewHolder(v!!) {
        val imageFrame: ImageView
        val tvTime: TextView

        init {
            imageFrame = itemView.findViewById(R.id.image_frame)
            tvTime = itemView.findViewById(R.id.tv_time)
            tvTime.setOnClickListener { view -> onFrameTimeClickListener.onItemClick(view, adapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FrameViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_frame, parent, false)
        return FrameViewHolder(view)
    }

    override fun onBindViewHolder(holder: FrameViewHolder, position: Int) {
        val frame = frames!![position]
        val minutes = frame.minutes
        val timeStr = Utils.minutesToStringTime(minutes)
        holder.tvTime.text = timeStr
        val dir = frame.filePath.substring(0, frame.filePath.lastIndexOf('_'))
        Glide.with(context)
                .load(Uri.parse("file:///android_asset/dwp/$dir/${frame.filePath}"))
                .dontAnimate()
                .centerCrop()
                .into(holder.imageFrame)
    }

    override fun getItemCount(): Int {
        return frames?.size ?: 0
    }

}
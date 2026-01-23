package com.ottplayerlite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ottplayerlite.utils.EpgDecorator
import java.util.*

class ChannelAdapter(
    private val channels: List<Channel>,
    private val onClick: (Channel) -> Unit
) : RecyclerView.Adapter<ChannelAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.channelName)
        val logo: ImageView = view.findViewById(R.id.channelLogo)
        val progress: ProgressBar = view.findViewById(R.id.channelProgress)
        val extraInfo: TextView = view.findViewById(R.id.epgExtraInfo) // Dodaj to do item_channel.xml!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_channel, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val channel = channels[position]
        holder.name.text = channel.name
        
        // 1. Detale EPG (Sezon, Odcinek, Rok) przez nasz Decorator
        val details = EpgDecorator.parseExtraInfo(channel.currentProgramDesc ?: "")
        if (details.isNotEmpty()) {
            holder.extraInfo.text = details
            holder.extraInfo.visibility = View.VISIBLE
        } else {
            holder.extraInfo.visibility = View.GONE
        }

        // 2. Realny pasek postępu EPG
        val progressPercent = calculateProgress(channel.startTime, channel.endTime)
        if (progressPercent in 1..99) {
            holder.progress.visibility = View.VISIBLE
            holder.progress.progress = progressPercent
        } else {
            holder.progress.visibility = View.INVISIBLE
        }
        
        Glide.with(holder.itemView.context)
            .load(channel.logoUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.logo)

        holder.itemView.setOnClickListener { onClick(channel) }
    }

    private fun calculateProgress(start: Long, end: Long): Int {
        if (start == 0L || end == 0L) return 0
        val now = System.currentTimeMillis()
        if (now < start) return 0
        if (now > end) return 100
        val total = end - start
        val elapsed = now - start
        return ((elapsed.toFloat() / total.toFloat()) * 100).toInt()
    }

    override fun getItemCount() = channels.size
}

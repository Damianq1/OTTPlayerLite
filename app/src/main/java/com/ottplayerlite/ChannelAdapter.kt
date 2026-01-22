package com.ottplayerlite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ChannelAdapter(
    private val channels: List<Channel>,
    private val onClick: (Channel) -> Unit
) : RecyclerView.Adapter<ChannelAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.channelName)
        val group: TextView = view.findViewById(R.id.channelGroup)
        val logo: ImageView = view.findViewById(R.id.channelLogo)
        
        init {
            // To sprawia, że wiersz reaguje na pilota
            view.isFocusable = true
            view.isFocusableInTouchMode = true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_channel, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val channel = channels[position]
        holder.name.text = channel.name
        holder.group.text = channel.group
        
        Glide.with(holder.itemView.context)
            .load(channel.logo)
            .placeholder(android.R.drawable.ic_menu_report_image)
            .into(holder.logo)

        holder.itemView.setOnClickListener { onClick(channel) }
        
        // Obsługa podświetlenia dla pilota (wizualna zmiana)
        holder.itemView.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                v.setBackgroundColor(android.graphics.Color.parseColor("#444444"))
                v.scaleX = 1.05f
                v.scaleY = 1.05f
            } else {
                v.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                v.scaleX = 1.0f
                v.scaleY = 1.0f
            }
        }
    }

    override fun getItemCount() = channels.size
}

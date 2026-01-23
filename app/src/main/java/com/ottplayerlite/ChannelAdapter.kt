package com.ottplayerlite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class ChannelAdapter(
    private val channels: List<Channel>,
    private val onClick: (Channel) -> Unit
) : RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder>() {

    class ChannelViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.channelName)
        val group: TextView = view.findViewById(R.id.channelGroup)
        val logo: ImageView = view.findViewById(R.id.channelLogo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_channel, parent, false)
        return ChannelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        val channel = channels[position]
        holder.name.text = channel.name
        holder.group.text = channel.group

        // Ładowanie logotypu z cache'owaniem, żeby nie marnować danych
        Glide.with(holder.itemView.context)
            .load(channel.logo)
            .placeholder(android.R.drawable.ic_menu_report_image) // Ikona podczas ładowania
            .error(android.R.drawable.ic_menu_gallery)          // Ikona jeśli link do loga wygasł
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.logo)

        holder.itemView.setOnClickListener { onClick(channel) }
    }

    override fun getItemCount() = channels.size
}

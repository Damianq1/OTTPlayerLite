package com.ottplayerlite.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ottplayerlite.R
import com.ottplayerlite.models.Channel
import com.bumptech.glide.Glide
import com.ottplayerlite.Logger

class ChannelAdapter(
    private val items: List<Channel>,
    private val onClick: (Channel) -> Unit
) : RecyclerView.Adapter<ChannelAdapter.Holder>() {

    class Holder(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.channelName)
        val group: TextView = v.findViewById(R.id.channelGroup)
        val logo: ImageView = v.findViewById(R.id.channelLogo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_channel, parent, false)
        )
    }

    override fun onBindViewHolder(h: Holder, pos: Int) {
        val c = items[pos]
        h.name.text = c.name
        h.group.text = c.group

        if (!c.logo.isNullOrEmpty()) {
            Glide.with(h.logo.context)
                .load(c.logo)
                .placeholder(R.drawable.ic_placeholder)
                .into(h.logo)
        } else {
            h.logo.setImageResource(R.drawable.ic_placeholder)
        }

        h.itemView.setOnClickListener { onClick(c) }
    }

    override fun getItemCount() = items.size
}

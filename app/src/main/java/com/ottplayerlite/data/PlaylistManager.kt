package com.ottplayerlite.data

import android.content.Context
import com.ottplayerlite.models.Channel
import com.ottplayerlite.utils.M3UParser

object PlaylistManager {
    private const val PREFS = "playlists"

    fun addPlaylist(ctx: Context, url: String) {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val set = prefs.getStringSet("urls", emptySet())?.toMutableSet() ?: mutableSetOf()
        set.add(url)
        prefs.edit().clear().putStringSet("urls", set).apply() 
    }

    fun getPlaylists(ctx: Context): List<String> {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getStringSet("urls", emptySet())?.toList() ?: emptyList()
    }

    fun loadAllChannels(ctx: Context): List<Channel> {
        val urls = getPlaylists(ctx)
        val allChannels = mutableListOf<Channel>()
        urls.forEach { url ->
            val channels = M3UParser.parse(source = url, context = ctx)
            allChannels.addAll(channels)
        }
        return allChannels
    }
}

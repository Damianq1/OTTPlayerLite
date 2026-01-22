package com.ottplayerlite.utils

import android.content.Context
import com.ottplayerlite.Channel
import com.ottplayerlite.ModuleManager
import java.net.HttpURLConnection
import java.net.URL

object M3UParser {
    fun fetchAndParse(context: Context, url: String): List<Channel> {
        val connection = URL(url).openConnection() as HttpURLConnection
        
        if (ModuleManager.isEnabled(context, "emulation")) {
            // Używamy zaktualizowanej metody z UserAgentManager
            val agent = UserAgentManager.getStringAgent(context)
            connection.setRequestProperty("User-Agent", agent)
            connection.setRequestProperty("X-User-MAC", "00:1A:79:00:00:00")
        }

        val content = connection.inputStream.bufferedReader().use { it.readText() }
        val list = mutableListOf<Channel>()
        var name = ""; var logo = ""; var group = ""
        
        content.lineSequence().forEach { line ->
            if (line.startsWith("#EXTINF")) {
                name = line.substringAfter(",").trim()
                logo = line.substringAfter("tvg-logo=\"", "").substringBefore("\"", "")
                group = line.substringAfter("group-title=\"", "").substringBefore("\"", "")
            } else if (line.startsWith("http")) {
                list.add(Channel(name, line.trim(), logo, group))
            }
        }
        return list
    }
}

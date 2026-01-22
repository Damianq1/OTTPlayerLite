package com.ottplayerlite.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.ottplayerlite.models.Channel
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object M3UParser {
    private const val DEFAULT_GROUP = "Inne"

    fun parse(source: String, context: Context? = null, localUri: Uri? = null): List<Channel> {
        return when {
            localUri != null && context != null -> parseLocal(context, localUri)
            source.startsWith("http", true) -> parseFromUrl(source, context)
            else -> emptyList()
        }
    }

    private fun parseFromUrl(url: String, context: Context?): List<Channel> {
        return try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 15000
            // Pobieramy User-Agent z Twojego Managera!
            val ua = context?.let { UserAgentManager.getUserAgent(it) } ?: "OTTPlayerLite/1.0"
            conn.setRequestProperty("User-Agent", ua)
            
            conn.inputStream.bufferedReader().use { reader ->
                parseFromReader(reader)
            }
        } catch (e: Exception) {
            Log.e("PARSER", "Błąd URL: ${e.message}")
            emptyList()
        }
    }

    private fun parseLocal(context: Context, uri: Uri): List<Channel> {
        return try {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
                parseFromReader(reader)
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseFromReader(reader: BufferedReader): List<Channel> {
        val list = mutableListOf<Channel>()
        var name = ""
        var group = DEFAULT_GROUP
        var logo: String? = null

        reader.forEachLine { line ->
            val l = line.trim()
            if (l.isEmpty()) return@forEachLine
            
            if (l.startsWith("#EXTINF", true)) {
                val n = Regex("tvg-name=\"(.*?)\"").find(l)?.groupValues?.get(1)
                val g = Regex("group-title=\"(.*?)\"").find(l)?.groupValues?.get(1)
                val lg = Regex("tvg-logo=\"(.*?)\"").find(l)?.groupValues?.get(1)
                val afterComma = l.substringAfter(",", "").trim()
                
                name = n ?: afterComma.ifEmpty { "Kanał" }
                group = g ?: DEFAULT_GROUP
                logo = lg
            } else if (l.startsWith("http") || l.startsWith("rtmp") || l.startsWith("rtsp")) {
                list.add(Channel(name, l, group, logo))
            }
        }
        return list
    }
}

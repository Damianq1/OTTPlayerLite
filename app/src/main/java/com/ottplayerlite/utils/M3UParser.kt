package com.ottplayerlite.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.ottplayerlite.models.Channel
import java.io.BufferedReader
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
            val ua = context?.let { UserAgentManager.getUserAgent(it) } ?: "OTTPlayerLite/1.0"
            conn.setRequestProperty("User-Agent", ua)
            conn.inputStream.bufferedReader().use { parseFromReader(it) }
        } catch (e: Exception) {
            Log.e("PARSER", "Błąd URL: ${e.message}")
            emptyList()
        }
    }

    private fun parseLocal(context: Context, uri: Uri): List<Channel> {
        return try {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { parseFromReader(it) } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseFromReader(reader: BufferedReader): List<Channel> {
        val list = mutableListOf<Channel>()
        var currentName = ""
        var currentGroup = DEFAULT_GROUP
        var currentLogo: String? = null

        reader.forEachLine { line ->
            val l = line.trim()
            if (l.isEmpty()) return@forEachLine

            if (l.startsWith("#EXTINF", true)) {
                // Wyciąganie nazwy (najpierw szukamy tvg-name, potem po przecinku)
                val tvgName = Regex("""tvg-name="([^"]*)"""").find(l)?.groupValues?.get(1)
                val groupTitle = Regex("""group-title="([^"]*)"""").find(l)?.groupValues?.get(1)
                val tvgLogo = Regex("""tvg-logo="([^"]*)"""").find(l)?.groupValues?.get(1)
                
                val nameAfterComma = l.substringAfterLast(",").trim()
                
                currentName = tvgName ?: if (nameAfterComma.isNotEmpty()) nameAfterComma else "Brak nazwy"
                currentGroup = groupTitle ?: DEFAULT_GROUP
                currentLogo = tvgLogo
            } else if (!l.startsWith("#")) {
                // Jeśli linia nie zaczyna się od #, zakładamy, że to URL kanału
                if (currentName.isNotEmpty()) {
                    list.add(Channel(currentName, l, currentGroup, currentLogo))
                    // Czyścimy nazwę, by nie dodać tego samego adresu dwa razy, jeśli lista jest błędna
                    currentName = "" 
                    currentGroup = DEFAULT_GROUP
                    currentLogo = null
                }
            }
        }
        Log.d("PARSER", "Zakończono parsowanie. Znaleziono ${list.size} kanałów.")
        return list
    }
}

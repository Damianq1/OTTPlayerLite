package com.ottplayerlite.data

import com.ottplayerlite.Channel
import com.ottplayerlite.utils.M3UParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

object PlaylistManager {
    /**
     * Pobiera zawartość listy M3U z sieci i przepuszcza przez nasz Ultimate Parser.
     */
    suspend fun fetchAndParse(url: String): List<Channel> = withContext(Dispatchers.IO) {
        return@withContext try {
            val content = URL(url).readText()
            M3UParser.parse(content)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList<Channel>()
        }
    }
}

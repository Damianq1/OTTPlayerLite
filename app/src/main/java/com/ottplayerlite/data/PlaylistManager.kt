package com.ottplayerlite.data

import com.ottplayerlite.Channel
import com.ottplayerlite.utils.M3UParser
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PlaylistManager {
    suspend fun fetchAndParse(url: String): List<Channel> = withContext(Dispatchers.IO) {
        try {
            val content = URL(url).readText()
            return@withContext M3UParser.parse(content)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList<Channel>()
        }
    }
}

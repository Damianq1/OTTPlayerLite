package com.ottplayerlite.data

import android.content.Context
import com.ottplayerlite.Channel
import com.ottplayerlite.utils.M3UParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PlaylistManager {
    suspend fun getPlaylist(context: Context, url: String): List<Channel> = withContext(Dispatchers.IO) {
        try {
            // Wywołujemy poprawną metodę z naszego parsera
            return@withContext M3UParser.fetchAndParse(context, url)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList<Channel>()
        }
    }
}

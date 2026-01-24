package com.ottplayerlite.utils

object CatchupManager {
    fun getArchiveUrl(baseUrl: String, timestamp: Long): String {
        // Teraz timestamp nie jest pusty - tworzy link do cofania TV
        val separator = if (baseUrl.contains("?")) "&" else "?"
        return "${baseUrl}${separator}utc=${timestamp}"
    }
}

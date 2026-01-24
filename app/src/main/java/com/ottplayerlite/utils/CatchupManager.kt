package com.ottplayerlite.utils

object CatchupManager {
    fun getArchiveUrl(baseUrl: String, timestamp: Long): String {
        // Zmienna timestamp jest teraz używana do generowania linku archiwum
        val separator = if (baseUrl.contains("?")) "&" else "?"
        return "${baseUrl}${separator}utc=${timestamp}&lutc=${System.currentTimeMillis() / 1000}"
    }
}

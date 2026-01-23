package com.ottplayerlite.utils

import java.text.SimpleDateFormat
import java.util.*

object CatchupManager {
    // Formatuje URL pod archiwum w zależności od dostawcy
    fun getArchiveUrl(baseUrl: String, startTime: Long, catchupType: String = "default"): String {
        val sdf = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault())
        val timestamp = sdf.format(Date(startTime))
        
        return when (catchupType) {
            "append" -> if (baseUrl.contains("?")) "$baseUrl&utc=$startTime" else "$baseUrl?utc=$startTime"
            "shift" -> "$baseUrl?timeshift=$startTime"
            else -> {
                // Automatyczna detekcja dla większości serwerów IPTV
                if (baseUrl.contains(".m3u8")) {
                    baseUrl.replace(".m3u8", ".m3u8?utc=$startTime")
                } else {
                    "$baseUrl?utc=$startTime"
                }
            }
        }
    }

    // Sprawdza czy dany kanał w ogóle wspiera archiwum
    fun hasCatchup(tags: Map<String, String>): Boolean {
        return tags.containsKey("catchup") || tags.containsKey("catchup-days")
    }
}

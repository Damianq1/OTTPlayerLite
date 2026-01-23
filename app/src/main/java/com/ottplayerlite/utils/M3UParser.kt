package com.ottplayerlite.utils

import com.ottplayerlite.Channel

object M3UParser {
    fun parse(m3uContent: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        val lines = m3uContent.lines()
        var currentInfo: String? = null

        for (line in lines) {
            if (line.startsWith("#EXTINF:")) {
                currentInfo = line
            } else if (line.startsWith("http") && currentInfo != null) {
                val name = currentInfo.substringAfterLast(",").trim()
                val logo = regexValue(currentInfo, "tvg-logo")
                val category = regexValue(currentInfo, "group-title")
                val tvgId = regexValue(currentInfo, "tvg-id")
                val catchup = regexValue(currentInfo, "catchup")

                channels.add(Channel(
                    name = name,
                    url = line.trim(),
                    logoUrl = logo,
                    category = category,
                    tvgId = tvgId,
                    catchupType = if (catchup.isNotEmpty()) catchup else "default"
                ))
                currentInfo = null
            }
        }
        return channels
    }

    private fun regexValue(line: String, key: String): String {
        val pattern = "$key=\"(.*?)\"".toRegex()
        return pattern.find(line)?.groupValues?.get(1) ?: ""
    }
}

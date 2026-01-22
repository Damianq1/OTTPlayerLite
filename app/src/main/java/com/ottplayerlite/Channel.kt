package com.ottplayerlite

import java.io.Serializable

data class Channel(
    val name: String,
    val url: String,
    val logo: String = "",
    val group: String = "",
    var bufferMs: Int = 3000, // Standardowo 3 sekundy
    var channelNum: Int = 0,
    var epgId: String? = null
) : Serializable

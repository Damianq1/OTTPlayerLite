package com.ottplayerlite

data class Channel(
    val name: String,
    val url: String,
    val logoUrl: String,
    val category: String,
    val tvgId: String = "",
    // Dane EPG dla paska postępu
    var currentProgramDesc: String? = null,
    var startTime: Long = 0,
    var endTime: Long = 0,
    var catchupType: String = "default"
)

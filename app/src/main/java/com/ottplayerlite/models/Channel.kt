package com.ottplayerlite.models

data class Channel(
    val name: String,
    val url: String,
    val group: String? = null,
    val logo: String? = null
)
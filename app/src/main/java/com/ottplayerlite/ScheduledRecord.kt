package com.ottplayerlite

import java.io.Serializable

data class ScheduledRecord(
    val channelName: String,
    val url: String,
    val startTime: Long, // Timestamp w ms
    val durationMin: Int,
    val id: Int = url.hashCode()
) : Serializable

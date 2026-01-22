package com.ottplayerlite.utils

import android.os.Environment
import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object LogHelper {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    fun log(context: Context, tag: String, message: String) {
        try {
            // Katalog dostępny dla Termuxa i Android TV
            val base = Environment.getExternalStorageDirectory()
            val dir = File(base, "OTTPlayerLite/logs")
            if (!dir.exists()) dir.mkdirs()

            val file = File(dir, "player.log")

            val ts = dateFormat.format(Date())
            file.appendText("[$ts][$tag] $message\n")
        } catch (_: Exception) {
            // Ignorujemy błędy logowania
        }
    }
}
package com.ottplayerlite

import android.content.Context
import java.io.File

object Logger {

    private lateinit var logFile: File

    fun init(context: Context) {
        val dir = context.getExternalFilesDir(null)
        logFile = File(dir, "ottplayer_log.txt")
        log("LOGGER INIT: path=${logFile.absolutePath}")
    }

    fun log(msg: String) {
        try {
            logFile.appendText(msg + "\n")
        } catch (e: Exception) {
            // nic nie rób
        }
    }
}
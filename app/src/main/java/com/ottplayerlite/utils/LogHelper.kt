package com.ottplayerlite.utils

import android.content.Context
import android.util.Log

object Logger {
    fun init(context: Context) {
        Log.d("OTT_APP", "Logger zainicjalizowany")
    }

    fun log(message: String) {
        Log.d("OTT_APP", message)
    }
}

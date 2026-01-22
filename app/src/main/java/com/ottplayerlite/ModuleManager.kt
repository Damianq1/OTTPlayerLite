package com.ottplayerlite

import android.content.Context

object ModuleManager {
    fun isEnabled(context: Context, moduleKey: String): Boolean {
        val prefs = context.getSharedPreferences("Modules", Context.MODE_PRIVATE)
        // Wersja LITE ma wszystko domyślnie wyłączone poza bazą
        if (BuildConfig.FLAVOR == "lite") return false
        return prefs.getBoolean(moduleKey, true)
    }

    fun setModuleStatus(context: Context, moduleKey: String, status: Boolean) {
        context.getSharedPreferences("Modules", Context.MODE_PRIVATE)
            .edit().putBoolean(moduleKey, status).apply()
    }
}

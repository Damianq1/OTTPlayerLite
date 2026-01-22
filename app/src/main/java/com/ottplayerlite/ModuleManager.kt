package com.ottplayerlite

import android.content.Context

object ModuleManager {
    fun isEnabled(context: Context, moduleKey: String): Boolean {
        // Sprawdzamy wersję po ID pakietu zamiast BuildConfig
        val isLite = context.packageName.endsWith(".lite")
        
        if (isLite) {
            // Blokada ciężkich modułów dla wersji Lite
            if (moduleKey == "stats" || moduleKey == "pip" || moduleKey == "remote" || moduleKey == "recording") {
                return false
            }
        }
        
        val prefs = context.getSharedPreferences("Modules", Context.MODE_PRIVATE)
        return prefs.getBoolean(moduleKey, true)
    }

    fun setModuleStatus(context: Context, moduleKey: String, status: Boolean) {
        context.getSharedPreferences("Modules", Context.MODE_PRIVATE)
            .edit().putBoolean(moduleKey, status).apply()
    }
}

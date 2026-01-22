package com.ottplayerlite

import android.content.Context

object ModuleManager {
    fun isEnabled(context: Context, moduleKey: String): Boolean {
        // Używamy pełnej ścieżki do wygenerowanej klasy
        if (com.ottplayerlite.BuildConfig.FLAVOR == "lite") {
            if (moduleKey == "stats" || moduleKey == "pip" || moduleKey == "remote") return false
        }
        
        val prefs = context.getSharedPreferences("Modules", Context.MODE_PRIVATE)
        return prefs.getBoolean(moduleKey, true)
    }

    fun setModuleStatus(context: Context, moduleKey: String, status: Boolean) {
        context.getSharedPreferences("Modules", Context.MODE_PRIVATE)
            .edit().putBoolean(moduleKey, status).apply()
    }
}

package com.ottplayerlite

import android.content.Context

object ModuleManager {
    private const val PREFS_NAME = "MODULE_SETTINGS"

    fun isEnabled(context: Context, moduleName: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(moduleName, true)
    }

    fun setEnabled(context: Context, moduleName: String, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(moduleName, enabled).apply()
    }
}

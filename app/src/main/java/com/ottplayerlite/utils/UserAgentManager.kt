package com.ottplayerlite.utils

import android.content.Context
import androidx.preference.PreferenceManager

object UserAgentManager {
    private const val KEY_MODE = "ua_mode"
    private const val KEY_CUSTOM = "ua_custom"
    private const val KEY_PLAYER_MODE = "player_mode"

    const val MODE_DEFAULT = 0
    const val MODE_VLC = 1
    const val MODE_CHROME = 2
    const val MODE_CUSTOM = 3

    const val PLAYER_AUTO = 0
    const val PLAYER_EXO = 1
    const val PLAYER_EXTERNAL = 2

    private const val UA_DEFAULT = "OTTPlayerLite/1.0"
    private const val UA_VLC = "VLC/3.0.16 LibVLC/3.0.16"
    private const val UA_CHROME = "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36"

    fun setMode(context: Context, mode: Int) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(KEY_MODE, mode).apply()
    }

    fun getUserAgent(context: Context): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return when (prefs.getInt(KEY_MODE, MODE_DEFAULT)) {
            MODE_VLC -> UA_VLC
            MODE_CHROME -> UA_CHROME
            MODE_CUSTOM -> prefs.getString(KEY_CUSTOM, UA_DEFAULT) ?: UA_DEFAULT
            else -> UA_DEFAULT
        }
    }

    fun setCustomUA(context: Context, ua: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_CUSTOM, ua).apply()
    }

    fun setPlayerMode(context: Context, mode: Int) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(KEY_PLAYER_MODE, mode).apply()
    }

    fun getPlayerMode(context: Context): Int {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_PLAYER_MODE, PLAYER_AUTO)
    }
}

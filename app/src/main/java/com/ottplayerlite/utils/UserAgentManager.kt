package com.ottplayerlite.utils

import android.content.Context

object UserAgentManager {
    private const val PREFS_NAME = "Settings"
    private const val KEY_UA = "user_agent"

    val AGENTS = mapOf(
        "Android TV (Default)" to "OTTPlayerPRO/1.0 (Android TV)",
        "MAG 250 (STB)" to "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 static Gecko/20100101 SmartTV/8.4.1",
        "Samsung SmartTV" to "Mozilla/5.0 (SmartHub; SMART-TV; Linux/SmartTV) AppleWebKit/538.1 (KHTML, like Gecko) SamsungBrowser/2.0 TV Safari/538.1",
        "Apple TV" to "AppleTV5,3/9.1.1"
    )

    fun getStringAgent(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_UA, AGENTS["Android TV (Default)"]) ?: AGENTS["Android TV (Default)"]!!
    }

    fun setAgent(context: Context, uaString: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_UA, uaString).apply()
    }
}

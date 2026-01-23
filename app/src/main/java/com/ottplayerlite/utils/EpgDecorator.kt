package com.ottplayerlite.utils

object EpgDecorator {
    fun parseExtraInfo(desc: String): String {
        val seasonRegex = "S[0-9]+E[0-9]+".toRegex()
        val yearRegex = "\\([0-9]{4}\\)".toRegex() // Poprawione escape'owanie
        
        val season = seasonRegex.find(desc)?.value ?: ""
        val year = yearRegex.find(desc)?.value ?: ""
        
        return if (season.isNotEmpty() || year.isNotEmpty()) "$season $year" else ""
    }
}

package com.ottplayerlite.utils

object EpgDecorator {
    fun parseExtraInfo(desc: String): String {
        // Szukamy wzorców typu S01E05 lub (2024)
        val seasonRegex = "S[0-9]+E[0-9]+".toRegex()
        val yearRegex = "\([0-9]{4}\)".toRegex()
        
        val season = seasonRegex.find(desc)?.value ?: ""
        val year = yearRegex.find(desc)?.value ?: ""
        
        return if (season.isNotEmpty() || year.isNotEmpty()) "$season $year" else ""
    }
}

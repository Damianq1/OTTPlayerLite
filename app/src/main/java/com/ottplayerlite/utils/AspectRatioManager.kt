package com.ottplayerlite.utils

import androidx.media3.ui.AspectRatioFrameLayout

object AspectRatioManager {
    private val modes = listOf(
        AspectRatioFrameLayout.RESIZE_MODE_FIT,
        AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH,
        AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT,
        AspectRatioFrameLayout.RESIZE_MODE_FILL,
        AspectRatioFrameLayout.RESIZE_MODE_ZOOM
    )
    
    private var currentModeIndex = 0

    fun getNextMode(): Int {
        currentModeIndex = (currentModeIndex + 1) % modes.size
        return modes[currentModeIndex]
    }
    
    fun getModeName(mode: Int): String = when(mode) {
        0 -> "FIT (Dopasuj)"
        1 -> "FIXED WIDTH"
        2 -> "FIXED HEIGHT"
        3 -> "STRETCH (Rozciągnij)"
        4 -> "ZOOM (Powiększ)"
        else -> "DEFAULT"
    }
}

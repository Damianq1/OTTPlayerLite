package com.ottplayerlite.utils

import android.content.Context
import android.view.Display
import android.view.WindowManager

object AFRManager {
    fun setHighRefreshRate(context: Context, window: android.view.Window) {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val modes = display.supportedModes
        
        // Szukamy najwyższego odświeżania (np. 120Hz)
        val highResMode = modes.maxByOrNull { it.refreshRate }
        
        val params = window.attributes
        params.preferredDisplayModeId = highResMode?.modeId ?: 0
        window.attributes = params
    }
}

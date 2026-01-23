package com.ottplayerlite.utils

import android.app.Activity
import android.view.WindowManager

object AFRManager {
    fun setHighRefreshRate(activity: Activity, window: android.view.Window) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val display = activity.display
            val supportedModes = display?.supportedModes
            // Szukamy trybu z najwyższym refreshRate (np. 120Hz)
            val maxMode = supportedModes?.maxByOrNull { it.refreshRate }
            
            val params = window.attributes
            params.preferredDisplayModeId = maxMode?.modeId ?: 0
            window.attributes = params
        }
    }
}

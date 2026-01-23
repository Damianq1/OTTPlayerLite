package com.ottplayerlite

data class PowerSettings(
    val actionType: String = "NONE", // NONE, CLOSE_APP, SCREEN_DIM, SHUTDOWN
    val minutes: Int = 30,
    val dimLevel: Int = 10 // Procent jasności
)

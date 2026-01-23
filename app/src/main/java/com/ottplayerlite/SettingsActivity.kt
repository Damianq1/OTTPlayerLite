package com.ottplayerlite

import android.content.Context
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("OTT_SETTINGS", Context.MODE_PRIVATE)
        val container = findViewById<LinearLayout>(R.id.modulesContainer)

        val vlcSwitch = Switch(this).apply {
            text = "Używaj silnika VLC (Lepsze kodeki)"
            isChecked = prefs.getBoolean("use_vlc", true)
            setTextColor(android.graphics.Color.WHITE)
            setOnCheckedChangeListener { _, isChecked ->
                prefs.edit().putBoolean("use_vlc", isChecked).apply()
            }
        }
        container.addView(vlcSwitch)
        
        findViewById<Button>(R.id.btnSaveSettings).setOnClickListener { finish() }
    }
}

package com.ottplayerlite

import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val radioGroup = findViewById<RadioGroup>(R.id.radioGroupEngine)
        val radioMedia3 = findViewById<RadioButton>(R.id.radioMedia3)
        val radioVlc = findViewById<RadioButton>(R.id.radioVlc)
        val radioWeb = findViewById<RadioButton>(R.id.radioWeb)
        val btnSave = findViewById<Button>(R.id.btnSaveSettings)

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val currentEngine = prefs.getString("engine", "MEDIA3")

        // Ustawienie zaznaczenia na starcie
        when (currentEngine) {
            "VLC" -> radioVlc.isChecked = true
            "WEB" -> radioWeb.isChecked = true
            else -> radioMedia3.isChecked = true
        }

        btnSave.setOnClickListener {
            val selectedEngine = when (radioGroup.checkedRadioButtonId) {
                R.id.radioVlc -> "VLC"
                R.id.radioWeb -> "WEB"
                else -> "MEDIA3"
            }
            prefs.edit().putString("engine", selectedEngine).apply()
            finish()
        }
    }
}

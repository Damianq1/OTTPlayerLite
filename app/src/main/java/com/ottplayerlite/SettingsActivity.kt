package com.ottplayerlite

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("Settings", MODE_PRIVATE)
        
        // Mapowanie switchy do kluczy ModuleManager
        val modules = mapOf(
            R.id.switchRec to "recording",
            R.id.switchRemote to "remote",
            R.id.switchVoice to "voice",
            R.id.switchEmulation to "emulation",
            R.id.switchPip to "pip",
            R.id.switchStats to "stats"
        )

        modules.forEach { (id, key) ->
            findViewById<SwitchMaterial>(id).apply {
                isChecked = ModuleManager.isEnabled(this@SettingsActivity, key)
                setOnCheckedChangeListener { _, isChecked ->
                    ModuleManager.setModuleStatus(this@SettingsActivity, key, isChecked)
                }
            }
        }

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }
}

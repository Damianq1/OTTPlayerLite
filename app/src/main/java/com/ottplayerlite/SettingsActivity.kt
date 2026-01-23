package com.ottplayerlite

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.format.Formatter
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("OTT_SETTINGS", Context.MODE_PRIVATE)
        val ipContainer = findViewById<LinearLayout>(R.id.ipContainer)
        val txtIp = findViewById<TextView>(R.id.txtDeviceIp)
        val editPort = findViewById<EditText>(R.id.editAdbPort)
        val btnSave = findViewById<Button>(R.id.btnSaveSettings)

        // Pobieranie adresu IP
        try {
            val wm = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ip = Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
            txtIp.text = "Twoje IP: $ip"
        } catch (e: Exception) {
            txtIp.text = "IP: Nie znaleziono"
        }

        // Wczytaj port
        editPort.setText(prefs.getInt("adb_port", 5555).toString())

        // Lista modułów do wygenerowania przełączników
        val modules = listOf("pip", "stats", "emulation", "remote")
        val container = findViewById<LinearLayout>(R.id.modulesContainer)

        modules.forEach { mod ->
            val sw = Switch(this).apply {
                text = "Moduł ${mod.uppercase()}"
                isChecked = ModuleManager.isEnabled(this@SettingsActivity, mod)
                setTextColor(android.graphics.Color.WHITE)
                setPadding(0, 10, 0, 10)
                
                setOnCheckedChangeListener { _, isChecked ->
                    ModuleManager.setEnabled(this@SettingsActivity, mod, isChecked)
                }
            }
            container.addView(sw)
        }

        btnSave.setOnClickListener {
            val port = editPort.text.toString().toIntOrNull() ?: 5555
            prefs.edit().putInt("adb_port", port).apply()
            Toast.makeText(this, "Zapisano. Restart ADB na porcie $port", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}

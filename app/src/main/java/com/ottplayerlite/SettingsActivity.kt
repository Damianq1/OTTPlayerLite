package com.ottplayerlite

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.format.Formatter
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("OTT_SETTINGS", Context.MODE_PRIVATE)
        val ipContainer = findViewById<LinearLayout>(R.id.ipContainer)
        val txtIp = findViewById<TextView>(R.id.txtDeviceIp)
        val editPort = findViewById<EditText>(R.id.editAdbPort)
        val btnSave = findViewById<Button>(R.id.btnSaveSettings)

        // Pobieranie IP urządzenia
        val ip = getDeviceIpAddress()
        txtIp.text = "IP Urządzenia: $ip"

        // Wczytywanie zapisanego portu (domyślnie 5555)
        val savedPort = prefs.getInt("adb_port", 5555)
        editPort.setText(savedPort.toString())

        // Przełączniki modułów (iteracja po Twoich modułach)
        val modules = listOf("pip", "stats", "emulation", "remote")
        val container = findViewById<LinearLayout>(R.id.modulesContainer)

        modules.forEach { mod ->
            val sw = Switch(this).apply {
                text = "Moduł: ${mod.uppercase()}"
                isChecked = ModuleManager.isEnabled(this@SettingsActivity, mod)
                setOnCheckedChangeListener { _, isChecked ->
                    ModuleManager.toggle(this@SettingsActivity, mod, isChecked)
                    if (mod == "remote") ipContainer.visibility = if (isChecked) android.view.View.VISIBLE else android.view.View.GONE
                }
            }
            container.addView(sw)
        }

        btnSave.setOnClickListener {
            val newPort = editPort.text.toString().toIntOrNull() ?: 5555
            prefs.edit().putInt("adb_port", newPort).apply()
            Toast.makeText(this, "Ustawienia zapisane", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getDeviceIpAddress(): String {
        val wm = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
    }
}

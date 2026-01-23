package com.ottplayerlite

import android.content.Context
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import com.ottplayerlite.utils.NetworkManager

class SettingsActivity : AppCompatActivity() {
    private val prefs by lazy { getSharedPreferences("OTT_DATA", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Sekcja VPN Status
        val vpnStatus = findViewById<TextView>(R.id.vpnStatus)
        if (NetworkManager.isVpnActive(this)) {
            vpnStatus.text = "VPN: AKTYWNY"
            vpnStatus.setTextColor(android.graphics.Color.GREEN)
        } else {
            vpnStatus.text = "VPN: NIEAKTYWNY"
            vpnStatus.setTextColor(android.graphics.Color.RED)
        }

        // Sekcja Proxy
        val switchProxy = findViewById<SwitchMaterial>(R.id.switchProxy)
        val proxyHost = findViewById<EditText>(R.id.proxyHost)
        val proxyPort = findViewById<EditText>(R.id.proxyPort)

        switchProxy.isChecked = prefs.getBoolean("use_proxy", false)
        proxyHost.setText(prefs.getString("proxy_host", ""))
        proxyPort.setText(prefs.getString("proxy_port", ""))

        findViewById<Button>(R.id.btnSaveProxy).setOnClickListener {
            prefs.edit().apply {
                putBoolean("use_proxy", switchProxy.isChecked)
                putString("proxy_host", proxyHost.text.toString())
                putString("proxy_port", proxyPort.text.toString())
                apply()
            }
            Toast.makeText(this, "Ustawienia sieciowe zapisane", Toast.LENGTH_SHORT).show()
        }
    }
}

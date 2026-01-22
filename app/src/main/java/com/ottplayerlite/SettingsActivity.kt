package com.ottplayerlite

import android.os.Bundle
import com.google.android.material.switchmaterial.SwitchMaterial
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.net.NetworkInterface

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val modules = mapOf(
            "recording" to "Nagrywanie i Harmonogram",
            "remote" to "Zdalne Sterowanie WWW",
            "voice" to "Wyszukiwanie Głosowe",
            "epg" to "Zaawansowane EPG i Paski Postępu",
            "emulation" to "Emulacja MAC/STB",
            "pip" to "Obraz w obrazie (PiP)", "stats" to "Statystyki Sygnału (Nerd Mode)"
        )
        // Tutaj pętla generująca UI lub statyczne powiązanie
        super.onCreate(savedInstanceState)
        val modules = mapOf(
            "recording" to "Nagrywanie i Harmonogram",
            "remote" to "Zdalne Sterowanie WWW",
            "voice" to "Wyszukiwanie Głosowe",
            "epg" to "Zaawansowane EPG i Paski Postępu",
            "emulation" to "Emulacja MAC/STB",
            "pip" to "Obraz w obrazie (PiP)", "stats" to "Statystyki Sygnału (Nerd Mode)"
        )
        // Tutaj pętla generująca UI lub statyczne powiązanie
        setContentView(R.layout.activity_settings)

        val infoText = findViewById<TextView>(R.id.zappingText)
        
        val mac = getMacAddress()
        val ip = java.net.InetAddress.getLocalHost().hostAddress ?: "N/A"
        
        infoText.text = "Adres MAC: $mac\nAdres IP: $ip\n\nSkonfiguruj buforowanie i listy poniżej."
    }

    private fun getMacAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces().toList()
            for (iface in interfaces) {
                if (iface.name.equals("wlan0", ignoreCase = true)) {
                    val mac = iface.hardwareAddress
                    return mac?.joinToString(":") { String.format("%02X", it) } ?: "Brak"
                }
            }
        } catch (e: Exception) { }
        return "Niedostępny"
    }
    private fun setupAgentSwitcher() {
        val agents = arrayOf("MAG 250 (STB)", "Samsung SmartTV", "Apple TV", "Android TV (Default)")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, agents)
        val spinner = findViewById<Spinner>(R.id.agentSpinner)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val selected = when(p2) {
                    0 -> "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 static Gecko/20100101 SmartTV/8.4.1"
                    1 -> "Mozilla/5.0 (SmartHub; SMART-TV; Linux/SmartTV) AppleWebkit/538.1 (KHTML, like Gecko) SamsungBrowser/2.0 TV Safari/538.1"
                    2 -> "AppleTV5,3/9.1.1"
                    else -> "OTTPlayerPRO/1.0 (Android TV)"
                }
                getSharedPreferences("Settings", MODE_PRIVATE).edit().putString("user_agent", selected).apply()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }
}

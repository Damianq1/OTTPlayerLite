package com.ottplayerlite

import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.ottplayerlite.utils.UserAgentManager

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupUserAgentSettings()
        setupPlayerSettings()
    }

    private fun setupUserAgentSettings() {
        val groupUA = findViewById<RadioGroup>(R.id.groupUserAgent)
        val customUAInput = findViewById<EditText>(R.id.editCustomUA)
        
        // Wczytaj aktualne ustawienia
        // Tutaj najlepiej byłoby dodać metodę getMode w UserAgentManager, 
        // ale na potrzeby UI użyjemy bezpośrednio SharedPreferences lub domyślnego zaznaczenia.

        groupUA.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.radioDefault -> UserAgentManager.MODE_DEFAULT
                R.id.radioVLC -> UserAgentManager.MODE_VLC
                R.id.radioChrome -> UserAgentManager.MODE_CHROME
                R.id.radioCustom -> UserAgentManager.MODE_CUSTOM
                else -> UserAgentManager.MODE_DEFAULT
            }
            UserAgentManager.setMode(this, mode)
        }

        // Zapisuj customowy UA przy każdej zmianie tekstu
        customUAInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                UserAgentManager.setCustomUA(this, customUAInput.text.toString())
            }
        }
    }

    private fun setupPlayerSettings() {
        val groupPlayer = findViewById<RadioGroup>(R.id.groupPlayerMode)
        
        groupPlayer.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.radioAuto -> UserAgentManager.PLAYER_AUTO
                R.id.radioExo -> UserAgentManager.PLAYER_EXO
                R.id.radioExternal -> UserAgentManager.PLAYER_EXTERNAL
                else -> UserAgentManager.PLAYER_AUTO
            }
            UserAgentManager.setPlayerMode(this, mode)
        }
    }
}

package com.ottplayerlite

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("OTT_DATA", Context.MODE_PRIVATE)
        val editHost = findViewById<EditText>(R.id.editHost)
        val editUser = findViewById<EditText>(R.id.editUser)
        val editPass = findViewById<EditText>(R.id.editPass)
        val btnSave = findViewById<Button>(R.id.btnSave)

        editHost.setText(prefs.getString("host", ""))
        editUser.setText(prefs.getString("user", ""))
        editPass.setText(prefs.getString("pass", ""))

        btnSave.setOnClickListener {
            prefs.edit().apply {
                putString("host", editHost.text.toString().trim())
                putString("user", editUser.text.toString().trim())
                putString("pass", editPass.text.toString().trim())
                apply()
            }
            Toast.makeText(this, "Konfiguracja zapisana", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}

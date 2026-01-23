package com.ottplayerlite

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*
import java.net.URL

class MainActivity : AppCompatActivity() {
    private var allChannels = listOf<Channel>()
    private lateinit var recyclerView: RecyclerView
    private val prefs by lazy { getSharedPreferences("OTT_DATA", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<TextInputEditText>(R.id.searchBar).addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filterList(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onResume() {
        super.onResume()
        loadPlaylist()
    }

    private fun loadPlaylist() {
        val host = prefs.getString("host", "") ?: ""
        if (host.isEmpty()) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = URL(host).readText()
                allChannels = parseM3U(data)
                withContext(Dispatchers.Main) { updateAdapter(allChannels) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Błąd linku. Sprawdź ustawienia.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun parseM3U(m3u: String): List<Channel> {
        val list = mutableListOf<Channel>()
        var name = ""; var logo = ""
        m3u.lineSequence().forEach { line ->
            if (line.startsWith("#EXTINF")) {
                name = line.substringAfter(",").trim()
                logo = line.substringAfter("tvg-logo=\"", "").substringBefore("\"", "")
            } else if (line.startsWith("http")) {
                list.add(Channel(name, line.trim(), logo, "TV"))
            }
        }
        return list
    }

    private fun filterList(query: String) {
        val filtered = allChannels.filter { it.name.contains(query, ignoreCase = true) }
        updateAdapter(filtered)
    }

    private fun updateAdapter(list: List<Channel>) {
        PlayerActivity.playlist = list
        recyclerView.adapter = ChannelAdapter(list) { channel ->
            startActivity(Intent(this, PlayerActivity::class.java).apply { putExtra("url", channel.url) })
        }
    }
}

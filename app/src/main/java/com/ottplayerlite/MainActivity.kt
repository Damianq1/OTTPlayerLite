package com.ottplayerlite

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import java.net.URL

class MainActivity : AppCompatActivity() {
    private var allChannels = listOf<Channel>()
    private var filteredChannels = listOf<Channel>()
    private lateinit var recyclerView: RecyclerView
    private val prefs by lazy { getSharedPreferences("OTT_DATA", Context.MODE_PRIVATE) }
    private var remoteServer: RemoteServer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Start serwera na porcie 8080
        try {
            remoteServer = RemoteServer(this, 8080)
            remoteServer?.start()
        } catch (e: Exception) { e.printStackTrace() }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        loadPlaylist()
    }

    private fun loadPlaylist() {
        val host = prefs.getString("host", "") ?: ""
        if (host.isEmpty()) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val content = URL(host).readText()
                allChannels = parseM3U(content)
                withContext(Dispatchers.Main) {
                    PlayerActivity.playlist = allChannels
                    recyclerView.adapter = ChannelAdapter(allChannels) { channel ->
                        startPlayer(channel.url)
                    }
                    
                    val lastUrl = prefs.getString("last_channel_url", "")
                    if (!lastUrl.isNullOrEmpty() && allChannels.any { it.url == lastUrl }) {
                        startPlayer(lastUrl)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Błąd listy", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startPlayer(url: String) {
        prefs.edit().putString("last_channel_url", url).apply()
        val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra("url", url)
        startActivity(intent)
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

    override fun onDestroy() {
        super.onDestroy()
        remoteServer?.stop()
    }
}

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
import com.ottplayerlite.utils.M3UParser
import com.ottplayerlite.data.PlaylistManager

class MainActivity : AppCompatActivity() {
    private var allChannels = listOf<Channel>()
    private lateinit var recyclerView: RecyclerView

    // Ujednolicony dostęp do ustawień
    private val prefs by lazy {
        getSharedPreferences("ULTIMATE_PREFS", Context.MODE_PRIVATE)
    }
    private var remoteServer: RemoteServer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Serwer zdalny (port 8080)
        startRemoteServer()

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        loadPlaylist()
    }

    private fun startRemoteServer() {
        try {
            remoteServer = RemoteServer(applicationContext, 8080)
            remoteServer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadPlaylist() {
        // Zmienione z 'host' na 'm3u_url' dla spójności z SettingsActivity
        val m3uUrl = prefs.getString("m3u_url", "") ?: ""
        if (m3uUrl.isEmpty()) {
            Toast.makeText(this, "Skonfiguruj URL w ustawieniach", Toast.LENGTH_LONG).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Używamy naszego nowego, potężnego parsera
                allChannels = PlaylistManager.fetchAndParse(m3uUrl)
                withContext(Dispatchers.Main) {
                    PlayerActivity.playlist = allChannels

                    val adapter = ChannelAdapter(allChannels) {
                        channel ->
                        startPlayer(channel.url)
                    }
                    recyclerView.adapter = adapter

                    // Funkcja Resume: Jeśli użytkownik włączył aplikację, wróć do ostatniego kanału
                    autoResumeLastChannel()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Błąd pobierania listy", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun autoResumeLastChannel() {
        val lastUrl = prefs.getString("last_channel_url", "")
        if (!lastUrl.isNullOrEmpty()) {
            val lastChannel = allChannels.find {
                it.url == lastUrl
            }
            if (lastChannel != null) {
                // Opcjonalnie: możesz dodać powiadomienie "Wznawianie ostatniego kanału..."
                startPlayer(lastUrl)
            }
        }
    }

    private fun startPlayer(url: String) {
        // Zapisujemy URL dla funkcji Resume
        prefs.edit().putString("last_channel_url", url).apply()

        val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra("url", url)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        // Odśwież listę przy powrocie z ustawień (jeśli URL się zmienił)
        loadPlaylist()
    }

    override fun onDestroy() {
        super.onDestroy()
        remoteServer?.stop()
    }
}
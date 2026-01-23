package com.ottplayerlite

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private var allChannels = listOf<Channel>()
    private lateinit var recyclerView: RecyclerView
    private val PREFS_NAME = "OTT_PREFS"
    private val LAST_URL_KEY = "last_m3u_url"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val urlInput = findViewById<TextInputEditText>(R.id.urlInput)
        val urlInputLayout = findViewById<TextInputLayout>(R.id.urlInputLayout)
        val btnLoad = findViewById<ExtendedFloatingActionButton>(R.id.btnLoad)

        // 1. WCZYTYWANIE OSTATNIEGO LINKU PRZY STARCIE
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedUrl = prefs.getString(LAST_URL_KEY, "")
        if (!savedUrl.isNullOrEmpty()) {
            urlInput.setText(savedUrl)
            loadPlaylist(savedUrl)
        }

        // OBSŁUGA SCHOWKA
        urlInputLayout?.setEndIconOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val item = clipboard.primaryClip?.getItemAt(0)
            urlInput.setText(item?.text?.toString() ?: "")
            Toast.makeText(this, "Wklejono", Toast.LENGTH_SHORT).show()
        }

        btnLoad.setOnClickListener {
            val url = urlInput.text.toString()
            if (url.isNotEmpty()) loadPlaylist(url)
        }
    }

    private fun loadPlaylist(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.connectTimeout = 8000
                val content = conn.inputStream.bufferedReader().use { it.readText() }
                allChannels = parseM3U(content)
                
                // 2. ZAPISYWANIE LINKU PO UDANYM POBRANIU
                getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit().putString(LAST_URL_KEY, url).apply()

                withContext(Dispatchers.Main) {
                    PlayerActivity.playlist = allChannels
                    recyclerView.adapter = ChannelAdapter(allChannels) { channel ->
                        startActivity(Intent(this@MainActivity, PlayerActivity::class.java).apply {
                            putExtra("url", channel.url)
                        })
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Błąd linku!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun parseM3U(m3u: String): List<Channel> {
        val list = mutableListOf<Channel>()
        var name = ""; var logo = ""; var group = ""
        m3u.lineSequence().forEach { line ->
            if (line.startsWith("#EXTINF")) {
                name = line.substringAfter(",").trim()
                logo = line.substringAfter("tvg-logo=\"", "").substringBefore("\"", "")
                group = line.substringAfter("group-title=\"", "").substringBefore("\"", "")
            } else if (line.startsWith("http")) {
                list.add(Channel(name, line.trim(), logo, group))
            }
        }
        return list
    }
}

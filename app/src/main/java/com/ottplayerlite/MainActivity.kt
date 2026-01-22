package com.ottplayerlite

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val urlInput = findViewById<EditText>(R.id.urlInput)
        val btnLoad = findViewById<Button>(R.id.btnLoad)
        val btnSettings = findViewById<Button>(R.id.btnSettings)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)

        btnLoad.setOnClickListener {
            val url = urlInput.text.toString()
            if (url.isNotEmpty()) {
                loadPlaylist(url, recyclerView)
            }
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun loadPlaylist(url: String, rv: RecyclerView) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val content = URL(url).readText()
                val channels = parseM3U(content)
                withContext(Dispatchers.Main) {
                    // Tutaj podepniemy Twój adapter (musisz go mieć w projekcie)
                    // rv.adapter = ChannelAdapter(channels) { channel ->
                    //    val intent = Intent(this@MainActivity, PlayerActivity::class.java)
                    //    intent.putExtra("url", channel.url)
                    //    startActivity(intent)
                    // }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun parseM3U(m3u: String): List<Pair<String, String>> {
        val list = mutableListOf<Pair<String, String>>()
        var name = "Nieznany kanał"
        m3u.lineSequence().forEach { line ->
            if (line.startsWith("#EXTINF")) {
                name = line.substringAfter(",").trim()
            } else if (line.startsWith("http")) {
                list.add(name to line.trim())
            }
        }
        return list
    }
}

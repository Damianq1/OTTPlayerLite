package com.ottplayerlite

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.speech.RecognizerIntent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private var allChannels = listOf<Channel>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var groupSpinner: Spinner
    private var remoteServer: RemoteServer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        groupSpinner = findViewById(R.id.groupSpinner)
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<Button>(R.id.btnLoad).setOnClickListener {
            val url = findViewById<EditText>(R.id.urlInput).text.toString()
            if (url.isNotEmpty()) loadPlaylist(url)
        }

        findViewById<ImageButton>(R.id.btnVoice).setOnClickListener {
            if (ModuleManager.isEnabled(this, "voice")) startVoiceSearch()
        }

        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<Button>(R.id.btnFile).setOnClickListener { scanLocalMedia() }

        if (ModuleManager.isEnabled(this, "remote")) {
            remoteServer = RemoteServer(this, 8080).apply { start() }
        }
    }

    private fun startVoiceSearch() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        try { startActivityForResult(intent, 102) } catch (e: Exception) { }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 102 && resultCode == RESULT_OK) {
            val query = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
            filterChannels(query)
        }
    }

    private fun loadPlaylist(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                if (ModuleManager.isEnabled(this@MainActivity, "emulation")) {
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (MAG200; STB)")
                    connection.setRequestProperty("X-User-MAC", "00:1A:79:00:00:00")
                }
                val content = connection.inputStream.bufferedReader().use { it.readText() }
                allChannels = parseM3U(content)
                withContext(Dispatchers.Main) { setupSpinnerAndList() }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Toast.makeText(this@MainActivity, "Błąd!", Toast.LENGTH_SHORT).show() }
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

    private fun setupSpinnerAndList() {
        val groups = listOf("Wszystkie") + allChannels.map { it.group }.distinct()
        groupSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, groups)
        groupSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                filterChannels(groupSpinner.selectedItem.toString())
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun filterChannels(query: String) {
        val filtered = if (query == "Wszystkie" || query.isEmpty()) allChannels 
                       else allChannels.filter { it.name.contains(query, true) || it.group == query }
        PlayerActivity.playlist = filtered
        recyclerView.adapter = ChannelAdapter(filtered) { channel ->
            startActivity(Intent(this, PlayerActivity::class.java).apply { putExtra("url", channel.url) })
        }
    }

    private fun scanLocalMedia() {
        CoroutineScope(Dispatchers.IO).launch {
            val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            val files = folder.walk().filter { it.extension == "mp4" }.map { 
                Channel(it.name, it.absolutePath, "", "Nagrania") 
            }.toList()
            withContext(Dispatchers.Main) {
                allChannels = files
                setupSpinnerAndList()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        remoteServer?.stop()
    }
}

package com.ottplayerlite

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import java.net.URL

class MainActivity : AppCompatActivity() {
    private var allChannels = listOf<Channel>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var groupSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val urlInput = findViewById<EditText>(R.id.urlInput)
        val btnLoad = findViewById<Button>(R.id.btnLoad)
        val btnSettings = findViewById<Button>(R.id.btnSettings)
        recyclerView = findViewById(R.id.recyclerView)
        groupSpinner = findViewById(R.id.groupSpinner)

        recyclerView.layoutManager = LinearLayoutManager(this)

        btnLoad.setOnClickListener {
            val url = urlInput.text.toString()
            if (url.isNotEmpty()) loadPlaylist(url)
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        groupSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                filterChannels(groupSpinner.selectedItem.toString())
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun loadPlaylist(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val content = URL(url).readText()
                allChannels = parseM3U(content)
                val groups = listOf("Wszystkie") + allChannels.map { it.group }.distinct().filter { it.isNotEmpty() }
                
                withContext(Dispatchers.Main) {
                    val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, groups)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    groupSpinner.adapter = adapter
                    filterChannels("Wszystkie")
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun filterChannels(group: String) {
        val filtered = if (group == "Wszystkie") allChannels else allChannels.filter { it.group == group }
        recyclerView.adapter = ChannelAdapter(filtered) { channel ->
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("url", channel.url)
            startActivity(intent)
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
                if (name.isNotEmpty()) {
                    list.add(Channel(name, line.trim(), logo, group))
                    name = ""; logo = ""; group = ""
                }
            }
        }
        return list
    }
}

package com.ottplayerlite

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ottplayerlite.data.PlaylistManager
import com.ottplayerlite.models.Channel
import com.ottplayerlite.ui.ChannelAdapter
import com.ottplayerlite.utils.M3UParser
import com.ottplayerlite.utils.Logger

class MainActivity : AppCompatActivity() {
    private lateinit var recycler: RecyclerView
    private lateinit var categoryContainer: LinearLayout
    private var allChannels: List<Channel> = emptyList()
    private var groups: List<String> = emptyList()

    // Obsługa wybierania pliku M3U z pamięci telefonu
    private val filePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data ?: return@registerForActivityResult
            Thread {
                val channels = M3UParser.parse("", this, uri)
                runOnUiThread {
                    allChannels = channels
                    updateGroups()
                    updateList("Wszystkie")
                }
            }.start()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.init(applicationContext)
        setContentView(R.layout.activity_main)

        // Inicjalizacja widoków
        recycler = findViewById(R.id.recyclerChannels)
        categoryContainer = findViewById(R.id.categoryContainer)

        // KLUCZOWE: Ustawienie LayoutManagera (bez tego lista jest pusta!)
        recycler.layoutManager = LinearLayoutManager(this)

        // Przyciski
        findViewById<Button>(R.id.btnAddUrl).setOnClickListener { addPlaylistUrl() }
        findViewById<Button>(R.id.btnAddFile).setOnClickListener { pickPlaylistFile() }
        
        // Przycisk ustawień (jeśli istnieje w XML)
        findViewById<Button>(R.id.btnSettings)?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        loadChannels()
    }

    private fun addPlaylistUrl() {
        val input = EditText(this)
        input.hint = "http://twoja-lista.m3u"
        
        AlertDialog.Builder(this)
            .setTitle("Dodaj URL playlisty")
            .setView(input)
            .setPositiveButton("Dodaj") { _, _ ->
                val url = input.text.toString().trim()
                if (url.isNotEmpty()) {
                    PlaylistManager.addPlaylist(this, url)
                    loadChannels()
                }
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun pickPlaylistFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        filePicker.launch(intent)
    }

    private fun loadChannels() {
        Thread {
            val playlists = PlaylistManager.getPlaylists(this)
            val tempChannels = mutableListOf<Channel>()
            playlists.forEach { url ->
                try {
                    val channels = M3UParser.parse(url, this)
                    tempChannels.addAll(channels)
                } catch (e: Exception) {
                    Logger.log("Błąd: ${e.message}")
                }
            }
            runOnUiThread {
                allChannels = tempChannels
                updateGroups()
                updateList("Wszystkie")
            }
        }.start()
    }

    private fun updateGroups() {
        groups = listOf("Wszystkie") + allChannels.map { it.group ?: "Inne" }.distinct().sorted()
        categoryContainer.removeAllViews()
        
        groups.forEach { groupName ->
            val view = LayoutInflater.from(this).inflate(R.layout.item_category, categoryContainer, false) as TextView
            view.text = groupName
            view.setOnClickListener { updateList(groupName) }
            categoryContainer.addView(view)
        }
    }

    private fun updateList(groupName: String) {
        val filtered = if (groupName == "Wszystkie") {
            allChannels
        } else {
            allChannels.filter { it.group == groupName }
        }
        
        // Przypisanie adaptera do listy
        recycler.adapter = ChannelAdapter(filtered) { channel ->
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("url", channel.url)
            startActivity(intent)
        }
    }
}

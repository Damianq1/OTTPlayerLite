package com.ottplayerlite

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.ottplayerlite.data.PlaylistManager
import com.ottplayerlite.models.Channel
import com.ottplayerlite.ui.ChannelAdapter
import com.ottplayerlite.utils.M3UParser

class MainActivity : AppCompatActivity() {
    private lateinit var recycler: RecyclerView
    private lateinit var categoryContainer: LinearLayout
    private var allChannels: List<Channel> = emptyList()
    private var groups: List<String> = emptyList()
    private val PICK_M3U = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.init(applicationContext)
        setContentView(R.layout.activity_main)

        recycler = findViewById(R.id.recyclerChannels)
        categoryContainer = findViewById(R.id.categoryContainer)

        findViewById<Button>(R.id.btnAddUrl).setOnClickListener { addPlaylistUrl() }
        findViewById<Button>(R.id.btnAddFile).setOnClickListener { pickPlaylistFile() }

        PlaylistManager.loadAllChannels(this); loadChannels()
    }

    private fun addPlaylistUrl() {
        val input = android.widget.EditText(this)
        AlertDialog.Builder(this)
            .setTitle("Dodaj playlistę")
            .setView(input)
            .setPositiveButton("Dodaj") { _, _ ->
                val url = input.text.toString().trim()
                if (url.isNotEmpty()) {
                    PlaylistManager.addPlaylist(this, url)
                    PlaylistManager.loadAllChannels(this); loadChannels()
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
        startActivityForResult(intent, PICK_M3U)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_M3U && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
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

    private fun PlaylistManager.loadAllChannels(this); loadChannels() {
        Thread {
            // Pobieramy ostatnio dodany URL z managera
            val lastUrl = PlaylistManager.getPlaylists(this).lastOrNull() ?: ""
            if (lastUrl.isNotEmpty()) {
                val channels = M3UParser.parse(lastUrl)
                runOnUiThread {
                    allChannels = channels
                    updateGroups()
                    updateList("Wszystkie")
                }
            }
        }.start()
    }

    private fun updateGroups() {
        groups = listOf("Wszystkie") + allChannels.map { it.group ?: "Inne" }.distinct()
        categoryContainer.removeAllViews()
        groups.forEach { group ->
            val view = LayoutInflater.from(this).inflate(R.layout.item_category, categoryContainer, false) as TextView
            view.text = group
            view.setOnClickListener { updateList(group) }
            categoryContainer.addView(view)
        }
    }

    private fun updateList(group: String) {
        val filtered = if (group == "Wszystkie") allChannels else allChannels.filter { it.group == group }
        recycler.adapter = ChannelAdapter(filtered) { channel ->
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("url", channel.url)
            startActivity(intent)
        }
    }
}

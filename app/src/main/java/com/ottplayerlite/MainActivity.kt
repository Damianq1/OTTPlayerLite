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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
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

        val editHost = findViewById<EditText>(R.id.editHost)
        val editUser = findViewById<EditText>(R.id.editUser)
        val editPass = findViewById<EditText>(R.id.editPass)
        val searchBar = findViewById<TextInputEditText>(R.id.searchBar)
        val btnLoad = findViewById<ExtendedFloatingActionButton>(R.id.btnLoad)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)

        editHost.setText(prefs.getString("host", ""))
        editUser.setText(prefs.getString("user", ""))
        editPass.setText(prefs.getString("pass", ""))

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        btnLoad.setOnClickListener {
            val h = editHost.text.toString().trim()
            val u = editUser.text.toString().trim()
            val p = editPass.text.toString().trim()
            prefs.edit().putString("host", h).putString("user", u).putString("pass", p).apply()

            if (h.contains("/c/")) {
                loadStalker(h, u)
            } else if (h.contains("m3u")) {
                fetchData(h, "M3U")
            } else {
                val apiUrl = "$h/player_api.php?username=$u&password=$p&action=get_live_streams"
                fetchData(apiUrl, "XTREAM")
            }
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filterList(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun fetchData(urlStr: String, type: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = URL(urlStr).readText()
                allChannels = if (type == "XTREAM") parseXtream(data) else parseM3U(data)
                withContext(Dispatchers.Main) { updateAdapter(allChannels) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Toast.makeText(this@MainActivity, "Błąd wczytywania", Toast.LENGTH_SHORT).show() }
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
                list.add(Channel(name, line.trim(), logo, "M3U"))
            }
        }
        return list
    }

    private fun parseXtream(json: String): List<Channel> {
        val list = mutableListOf<Channel>()
        val arr = org.json.JSONArray(json)
        val h = prefs.getString("host", ""); val u = prefs.getString("user", ""); val p = prefs.getString("pass", "")
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val sUrl = "$h/live/$u/$p/${obj.getString("stream_id")}.ts"
            list.add(Channel(obj.getString("name"), sUrl, obj.optString("stream_icon"), "Live"))
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
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("url", channel.url)
            startActivity(intent)
        }
    }

    private fun loadStalker(h: String, u: String) { /* Logika stalker */ }
}

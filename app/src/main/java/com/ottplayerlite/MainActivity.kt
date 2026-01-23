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

        // Wczytywanie zapisanych danych
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

            when {
                // STALKER / MAC PORTAL
                h.contains("p2.iptvprivateserver.tv") || h.contains("/c/") -> loadStalker(h, u)
                
                // M3U / M3U8
                h.contains("m3u", ignoreCase = true) -> fetchData(h, "M3U")
                
                // XTREAM CODES
                else -> {
                    val apiUrl = "$h/player_api.php?username=$u&password=$p&action=get_live_streams"
                    fetchData(apiUrl, "XTREAM")
                }
            }
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filterList(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadStalker(host: String, mac: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Handshake dla Portalu MAC
                val handshakeUrl = "$host?type=stb&action=handshake"
                val conn = URL(handshakeUrl).openConnection() as HttpURLConnection
                conn.setRequestProperty("Cookie", "mac=$mac")
                val token = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                    .getJSONObject("js").getString("token")

                // Pobieranie kanałów
                val channelsUrl = "$host?type=itv&action=get_all_channels&token=$token"
                val channelData = stalkerRequest(channelsUrl, mac)
                allChannels = parseStalker(channelData)

                withContext(Dispatchers.Main) { updateAdapter(allChannels) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Toast.makeText(this@MainActivity, "Błąd Portalu MAC", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private fun stalkerRequest(urlStr: String, mac: String): String {
        val conn = URL(urlStr).openConnection() as HttpURLConnection
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (MAG200; STB)")
        conn.setRequestProperty("Cookie", "mac=$mac")
        return conn.inputStream.bufferedReader().use { it.readText() }
    }

    private fun parseStalker(json: String): List<Channel> {
        val list = mutableListOf<Channel>()
        val arr = JSONObject(json).getJSONArray("js")
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val url = obj.getString("cmd").replace("ffmpeg ", "")
            list.add(Channel(obj.getString("name"), url, obj.optString("logo"), "Stalker"))
        }
        return list
    }

    private fun fetchData(urlStr: String, type: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = URL(urlStr).readText()
                allChannels = if (type == "XTREAM") parseXtream(data) else parseM3U(data)
                withContext(Dispatchers.Main) { updateAdapter(allChannels) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Toast.makeText(this@MainActivity, "Błąd $type", Toast.LENGTH_SHORT).show() }
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

    private fun parseXtream(json: String): List<Channel> {
        val list = mutableListOf<Channel>()
        val arr = org.json.JSONArray(json)
        val h = prefs.getString("host", ""); val u = prefs.getString("user", ""); val p = prefs.getString("pass", "")
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val sUrl = "$h/live/$u/$p/${obj.getString("stream_id")}.ts"
            list.add(Channel(obj.getString("name"), sUrl, obj.optString("stream_icon"), obj.optString("category_name")))
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

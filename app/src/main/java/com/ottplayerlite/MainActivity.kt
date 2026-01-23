package com.ottplayerlite

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

        editHost.setText(prefs.getString("host", ""))
        editUser.setText(prefs.getString("user", ""))
        editPass.setText(prefs.getString("pass", ""))

        btnLoad.setOnClickListener {
            val h = editHost.text.toString().trim()
            val u = editUser.text.toString().trim()
            val p = editPass.text.toString().trim()
            prefs.edit().putString("host", h).putString("user", u).putString("pass", p).apply()

            when {
                h.contains("p2.iptvprivateserver.tv") || h.contains("/c/") -> loadStalker(h, u)
                h.contains("m3u") || h.contains("m3u8") -> fetchData(h, type = "M3U")
                else -> fetchData("$h/player_api.php?username=$u&password=$p&action=get_live_streams", type = "XTREAM")
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
                // 1. Handshake & Get Token
                val handshakeUrl = "$host?type=stb&action=handshake"
                val tokenJson = stalkerRequest(handshakeUrl, mac)
                val token = JSONObject(tokenJson).getJSONObject("js").getString("token")

                // 2. Get Channels
                val channelsUrl = "$host?type=itv&action=get_all_channels&token=$token"
                val data = stalkerRequest(channelsUrl, mac)
                allChannels = parseStalker(data, host, token)

                withContext(Dispatchers.Main) { updateAdapter(allChannels) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Toast.makeText(this@MainActivity, "Błąd MAC Portalu!", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private fun stalkerRequest(urlStr: String, mac: String): String {
        val conn = URL(urlStr).openConnection() as HttpURLConnection
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 sb2 embedded Safari/533.3")
        conn.setRequestProperty("X-User-Agent", "Model: MAG250; SW: 2.20.0")
        conn.setRequestProperty("Cookie", "mac=$mac")
        return conn.inputStream.bufferedReader().use { it.readText() }
    }

    private fun parseStalker(json: String, host: String, token: String): List<Channel> {
        val list = mutableListOf<Channel>()
        val arr = JSONObject(json).getJSONArray("js")
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val cmd = obj.getString("cmd").replace("ffmpeg ", "")
            list.add(Channel(obj.getString("name"), cmd, obj.optString("logo"), obj.optString("tvg_name")))
        }
        return list
    }

    private fun fetchData(url: String, type: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val conn = URL(url).openConnection() as HttpURLConnection
                val data = conn.inputStream.bufferedReader().use { it.readText() }
                allChannels = if (type == "XTREAM") parseXtream(data) else parseM3U(data)
                withContext(Dispatchers.Main) { updateAdapter(allChannels) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Toast.makeText(this@MainActivity, "Błąd $type!", Toast.LENGTH_SHORT).show() }
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

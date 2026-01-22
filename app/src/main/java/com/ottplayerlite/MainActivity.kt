package com.ottplayerlite

import android.Manifest
import android.content.Intent
import android.speech.RecognizerIntent
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import java.io.File
import java.net.URL

class MainActivity : AppCompatActivity() {
    private var allChannels = listOf<Channel>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var groupSpinner: Spinner

    private val filePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { readLocalFile(it) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 102 && resultCode == RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val query = result?.get(0) ?: ""
            filterChannels(query)
            Toast.makeText(this, "Szukasz: $query", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        val wm = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager 
        val lock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "OttLock") 
        if (getSharedPreferences("Settings", MODE_PRIVATE).getBoolean("lock_ip", false)) { 
            lock.acquire() 
        }
        super.onCreate(savedInstanceState)
        val wm = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager 
        val lock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "OttLock") 
        if (getSharedPreferences("Settings", MODE_PRIVATE).getBoolean("lock_ip", false)) { 
            lock.acquire() 
        }
        setContentView(R.layout.activity_main)

        val urlInput = findViewById<EditText>(R.id.urlInput)
        val btnLoad = findViewById<Button>(R.id.btnLoad)
        val btnFile = findViewById<Button>(R.id.btnFile)
        val btnSettings = findViewById<Button>(R.id.btnSettings)
        recyclerView = findViewById(R.id.recyclerView)
        groupSpinner = findViewById(R.id.groupSpinner)

        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<View>(R.id.btnVoice).setOnClickListener { startVoiceSearch() }
        btnLoad.setOnClickListener {
            val url = urlInput.text.toString()
            if (url.isNotEmpty()) loadPlaylist(getDeviceAuthUrl(url))
        }

        btnFile.setOnClickListener {
            // Tutaj wywołujemy skanowanie pamięci zamiast prostego wybieracza
            scanLocalMedia()
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
        
        checkPermissions()
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 101)
        }
    }

    private fun scanLocalMedia() {
        Toast.makeText(this, "Skanowanie pamięci...", Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.IO).launch {
            val mediaFiles = mutableListOf<Channel>()
            val folders = mutableListOf(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                File("/storage") // Próba znalezienia karty SD/USB
            )

            folders.forEach { folder ->
                if (folder.exists()) {
                    folder.walkTopDown().filter { it.isFile && listOf("mp4", "mkv", "ts", "avi").contains(it.extension.lowercase()) }.forEach {
                        mediaFiles.add(Channel(it.name, it.absolutePath, "", "Moje Nagrania"))
                    }
                }
            }

            withContext(Dispatchers.Main) {
                allChannels = mediaFiles
                setupSpinnerAndList()
                Toast.makeText(this@MainActivity, "Znaleziono ${mediaFiles.size} plików", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadPlaylist(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val connection = URL(url).openConnection() as java.net.HttpURLConnection
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 static Gecko/20100101 SmartTV/8.4.1")
                connection.setRequestProperty("X-User-MAC", "00:1A:79:XX:XX:XX") // Tutaj wpisz swój MAC z panelu
                val content = connection.inputStream.bufferedReader().use { it.readText() }
                allChannels = parseM3U(content)
                withContext(Dispatchers.Main) { setupSpinnerAndList() }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Toast.makeText(this@MainActivity, "Błąd linku!", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private fun setupSpinnerAndList() {
        val groups = listOf("Wszystkie") + allChannels.map { it.group }.distinct().filter { it.isNotEmpty() }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, groups)
        groupSpinner.adapter = adapter
        filterChannels("Wszystkie")
    }

    private fun filterChannels(group: String) {
        val filtered = if (group == "Wszystkie") allChannels else allChannels.filter { it.group == group }
        PlayerActivity.playlist = filtered
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

    private fun readLocalFile(uri: Uri) { /* implementacja jak wcześniej */ }
    private fun getDeviceAuthUrl(baseUrl: String): String {
        val mac = java.net.NetworkInterface.getNetworkInterfaces().toList().find { it.name.contains("wlan") }?.hardwareAddress?.joinToString(":") { String.format("%02X", it) } ?: "00:00:00:00:00:00"
        return "$baseUrl?mac=$mac&device=android"
    }
        if (!ModuleManager.isEnabled(this, "voice")) return
    private fun startVoiceSearch() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        try {
            startActivityForResult(intent, 102)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Głos nieobsługiwany", Toast.LENGTH_SHORT).show()
        }
    }
}

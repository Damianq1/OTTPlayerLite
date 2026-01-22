package com.ottplayerlite

import android.content.Context
import android.os.*
import android.app.PictureInPictureParams
import android.util.Rational
import android.content.res.Configuration
import android.view.KeyEvent
import android.view.View
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.AlarmManager
import android.app.PendingIntent
import android.widget.*
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.drawerlayout.widget.DrawerLayout
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@OptIn(UnstableApi::class)
class PlayerActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var tvName: TextView
    private lateinit var tvEpg: TextView
    private lateinit var progress: ProgressBar

    private var currentIndex = 0
    private var isRecording = false
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        var playlist: List<Channel> = listOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.playerView)
        drawerLayout = findViewById(R.id.drawerLayout)
        tvName = findViewById(R.id.channelName)
        tvEpg = findViewById(R.id.epgTitle)
        progress = findViewById(R.id.epgProgress)

        val url = intent.getStringExtra("url") ?: ""
        currentIndex = playlist.indexOfFirst { it.url == url }.coerceAtLeast(0)

        if (BuildConfig.FLAVOR == "lite") {
            // W wersji Lite wymuszamy minimalny bufor dla oszczędności RAM
            player.setLoadControl(DefaultLoadControl.Builder().setBufferMs(1000, 2000, 500, 500).build())
            btnRecord.visibility = View.GONE
        }
        playChannel(currentIndex)
    }

        if (BuildConfig.FLAVOR == "lite") {
            // W wersji Lite wymuszamy minimalny bufor dla oszczędności RAM
            player.setLoadControl(DefaultLoadControl.Builder().setBufferMs(1000, 2000, 500, 500).build())
            btnRecord.visibility = View.GONE
        }
    private fun playChannel(index: Int) {
        if (::player.isInitialized) player.release()
        
        val channel = playlist[index]
        
        // Punkt 17: Per-channel custom buffer
        val loadControl = DefaultLoadControl.Builder()
            .setBufferMs(channel.bufferMs, channel.bufferMs + 2000, 1000, 1500)
            .build()

        player = ExoPlayer.Builder(this)
            .setLoadControl(loadControl)
            .build()
        
        playerView.player = player
        player.setMediaItem(MediaItem.fromUri(channel.url))
        player.prepare()
        player.play()
        startStatsMonitor()

        // Punkt 16: Auto-reconnect on error
        player.addListener(object : androidx.media3.common.Player.Listener {
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
        if (BuildConfig.FLAVOR == "lite") {
            // W wersji Lite wymuszamy minimalny bufor dla oszczędności RAM
            player.setLoadControl(DefaultLoadControl.Builder().setBufferMs(1000, 2000, 500, 500).build())
            btnRecord.visibility = View.GONE
        }
                handler.postDelayed({ playChannel(currentIndex) }, 3000)
            }
        })

        updateUI(channel)
    }

    private fun updateUI(channel: Channel) {
        tvName.text = channel.name
        tvEpg.text = "Ładowanie programu..." 
        findViewById<View>(R.id.channelOverlay).visibility = View.VISIBLE
        
        // Punkt 6: Przykładowy postęp EPG (teraz symulacja)
        progress.progress = (10..90).random() 
        
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({ findViewById<View>(R.id.channelOverlay).visibility = View.GONE }, 5000)
    }

    // Punkt 12: Nagrywanie w tle (uproszczone do Coroutine, która żyje po OnStop)
    private fun startBackgroundRecording(channel: Channel) {
        isRecording = true
        Toast.makeText(this, "Nagrywanie w tle: ${channel.name}", Toast.LENGTH_SHORT).show()
        
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val file = File(getExternalFilesDir(null), "${channel.name}.mp4")
                val stream = URL(channel.url).openStream()
                val out = FileOutputStream(file)
                val buffer = ByteArray(1024 * 32)
                var read: Int
                while (isRecording && stream.read(buffer).also { read = it } != -1) {
                    out.write(buffer, 0, read)
                }
                out.close()
                stream.close()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                // Punkt 7: Szybkie menu pod OK
                drawerLayout.openDrawer(android.view.Gravity.LEFT)
                return true
            }
        if (BuildConfig.FLAVOR == "lite") {
            // W wersji Lite wymuszamy minimalny bufor dla oszczędności RAM
            player.setLoadControl(DefaultLoadControl.Builder().setBufferMs(1000, 2000, 500, 500).build())
            btnRecord.visibility = View.GONE
        }
            KeyEvent.KEYCODE_DPAD_UP -> { currentIndex = (currentIndex + 1) % playlist.size; playChannel(currentIndex); return true }
        if (BuildConfig.FLAVOR == "lite") {
            // W wersji Lite wymuszamy minimalny bufor dla oszczędności RAM
            player.setLoadControl(DefaultLoadControl.Builder().setBufferMs(1000, 2000, 500, 500).build())
            btnRecord.visibility = View.GONE
        }
            KeyEvent.KEYCODE_DPAD_DOWN -> { currentIndex = (currentIndex - 1 + playlist.size) % playlist.size; playChannel(currentIndex); return true }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::player.isInitialized) player.release()
    }
    fun scheduleRecording(minutesFromNow: Int, duration: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, RecordReceiver::class.java).apply {
            putExtra("name", playlist[currentIndex].name)
            putExtra("url", playlist[currentIndex].url)
            putExtra("duration", duration)
        }
        val pendingIntent = PendingIntent.getBroadcast(this, playlist[currentIndex].url.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val triggerTime = System.currentTimeMillis() + (minutesFromNow * 60 * 1000)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        Toast.makeText(this, "Nagranie zaplanowane!", Toast.LENGTH_SHORT).show()
    }
    fun scheduleRecording(minutesFromNow: Int, duration: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, RecordReceiver::class.java).apply {
            putExtra("name", playlist[currentIndex].name)
            putExtra("url", playlist[currentIndex].url)
            putExtra("duration", duration)
        }
        val pendingIntent = PendingIntent.getBroadcast(this, playlist[currentIndex].url.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val triggerTime = System.currentTimeMillis() + (minutesFromNow * 60 * 1000)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        Toast.makeText(this, "Nagranie zaplanowane!", Toast.LENGTH_SHORT).show()
    }
}

    override fun onUserLeaveHint() { 
        if (ModuleManager.isEnabled(this, "pip")) { 
            val aspectRatio = Rational(16, 9) 
            val params = PictureInPictureParams.Builder() 
                .setAspectRatio(aspectRatio) 
                .build() 
            enterPictureInPictureMode(params) 
        } 
    }
    if (ModuleManager.isEnabled(this, "pip")) {
        val params = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))
            .build()
        enterPictureInPictureMode(params)
    }
}

override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
    if (isInPictureInPictureMode) {
        // Ukryj interfejs (przyciski, pasek postępu), zostaw tylko wideo
        supportActionBar?.hide()
        playerControls.visibility = View.GONE
    } else {
        // Przywróć interfejs po powrocie do pełnego ekranu
        supportActionBar?.show()
        playerControls.visibility = View.VISIBLE
    }
}

private fun startStatsMonitor() {
    if (!ModuleManager.isEnabled(this, "stats")) return
    findViewById<View>(R.id.statsContainer).visibility = View.VISIBLE
    
    handler.post(object : Runnable {
        override fun run() {
            val format = player.videoFormat
            if (format != null) {
                val fps = format.frameRate.toInt()
                val bitrate = (player.videoDecoderCounters?.renderedOutputBufferCount ?: 0) // Uproszczony wskaźnik
                
                val txtFps = findViewById<TextView>(R.id.txtFps)
                val txtBitrate = findViewById<TextView>(R.id.txtBitrate)
                
                txtFps.text = "FPS: $fps"
                txtResolution.text = "Res: ${format.width}x${format.height}"
                
                // Logika alarmu: Zmiana koloru na czerwony przy problemach
                if (fps < 10 || !player.isPlaying) {
                    txtFps.setTextColor(android.graphics.Color.RED)
                    txtBitrate.setTextColor(android.graphics.Color.RED)
                } else {
                    txtFps.setTextColor(android.graphics.Color.GREEN)
                    txtBitrate.setTextColor(android.graphics.Color.GREEN)
                }
            }
            handler.postDelayed(this, 1000)
        }
    })
}

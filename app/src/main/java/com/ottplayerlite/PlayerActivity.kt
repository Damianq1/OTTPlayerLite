package com.ottplayerlite

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.graphics.Color
import android.os.*
import android.util.Rational
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlinx.coroutines.*

class PlayerActivity : AppCompatActivity() {
    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private var currentIndex = 0
    private val handler = Handler(Looper.getMainLooper())

    companion object { var playlist: List<Channel> = listOf() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        playerView = findViewById(R.id.playerView)
        
        val url = intent.getStringExtra("url") ?: ""
        currentIndex = playlist.indexOfFirst { it.url == url }.coerceAtLeast(0)
        
        playChannel(currentIndex)
    }

    private fun playChannel(index: Int) {
        player?.release()
        currentIndex = index
        val channel = playlist[index]
        
        // Używamy Factory, która automatycznie obsłuży HLS (.m3u8) i DASH
        val mediaSourceFactory = DefaultMediaSourceFactory(this)
        
        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(channel.url))
                prepare()
                play()
            }
        playerView.player = player
        
        if (ModuleManager.isEnabled(this, "stats")) startStatsMonitor()
        showOverlay(channel)
    }

    private fun showOverlay(channel: Channel) {
        val overlay = findViewById<View>(R.id.channelOverlay) ?: return
        findViewById<TextView>(R.id.channelName)?.text = channel.name
        overlay.visibility = View.VISIBLE
        handler.removeCallbacksAndMessages("overlay")
        handler.postAtTime({ overlay.visibility = View.GONE }, "overlay", SystemClock.uptimeMillis() + 5000)
    }

    private fun startStatsMonitor() {
        handler.post(object : Runnable {
            override fun run() {
                val format = player?.videoFormat
                if (format != null) {
                    findViewById<View>(R.id.statsContainer)?.visibility = View.VISIBLE
                    findViewById<TextView>(R.id.txtResolution)?.text = "Res: ${format.width}x${format.height}"
                    findViewById<TextView>(R.id.txtFps)?.apply {
                        val fps = format.frameRate.toInt()
                        text = "FPS: $fps"
                        setTextColor(if (fps < 10) Color.RED else Color.GREEN)
                    }
                }
                handler.postDelayed(this, 2000)
            }
        })
    }

    override fun onUserLeaveHint() {
        if (ModuleManager.isEnabled(this, "pip")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                enterPictureInPictureMode(PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .build())
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> { playChannel((currentIndex + 1) % playlist.size); return true }
            KeyEvent.KEYCODE_DPAD_DOWN -> { playChannel((currentIndex - 1 + playlist.size) % playlist.size); return true }
            KeyEvent.KEYCODE_BACK -> { finish(); return true }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        handler.removeCallbacksAndMessages(null)
    }
}

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
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
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

    companion object { 
        var playlist: List<Channel> = listOf() 
    }

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
        if (playlist.isEmpty()) return
        
        currentIndex = index
        val channel = playlist[index]
        
        // 1. Konfiguracja renderowania (HW + SW fallback dla kanałów z czarnym ekranem)
        val renderersFactory = DefaultRenderersFactory(this)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)

        // 2. Konfiguracja źródła danych z User-Agent (żeby serwer nie odrzucał połączenia)
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .setAllowCrossProtocolRedirects(true)

        val mediaSourceFactory = DefaultMediaSourceFactory(this)
            .setDataSourceFactory(dataSourceFactory)

        // 3. Budowa Playera
        player = ExoPlayer.Builder(this, renderersFactory)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(channel.url))
                prepare()
                play()
            }
            
        playerView.player = player
        
        // Listener błędów
        player?.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Toast.makeText(this@PlayerActivity, "Błąd odtwarzania: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        if (ModuleManager.isEnabled(this, "stats")) startStatsMonitor()
        showOverlay(channel)
    }

    private fun showOverlay(channel: Channel) {
        val overlay = findViewById<View>(R.id.channelOverlay) ?: return
        val nameText = findViewById<TextView>(R.id.channelName)
        nameText?.text = channel.name
        
        overlay.visibility = View.VISIBLE
        handler.removeCallbacksAndMessages("overlay_timer")
        handler.postAtTime({ overlay.visibility = View.GONE }, "overlay_timer", SystemClock.uptimeMillis() + 5000)
    }

    private fun startStatsMonitor() {
        handler.post(object : Runnable {
            override fun run() {
                val format = player?.videoFormat
                val statsContainer = findViewById<View>(R.id.statsContainer)
                if (format != null && statsContainer != null) {
                    statsContainer.visibility = View.VISIBLE
                    findViewById<TextView>(R.id.txtResolution)?.text = "Res: ${format.width}x${format.height}"
                    findViewById<TextView>(R.id.txtFps)?.apply {
                        val fps = format.frameRate.toInt()
                        text = "FPS: $fps"
                        setTextColor(if (fps <= 0) Color.RED else Color.GREEN)
                    }
                }
                handler.postDelayed(this, 2000)
            }
        })
    }

    override fun onUserLeaveHint() {
        if (ModuleManager.isEnabled(this, "pip")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .build()
                enterPictureInPictureMode(params)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> { 
                val next = (currentIndex + 1) % playlist.size
                playChannel(next)
                return true 
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> { 
                val prev = if (currentIndex - 1 < 0) playlist.size - 1 else currentIndex - 1
                playChannel(prev)
                return true 
            }
            KeyEvent.KEYCODE_BACK -> { 
                if (playerView.isControllerFullyVisible) {
                    playerView.hideController()
                } else {
                    finish()
                }
                return true 
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        handler.removeCallbacksAndMessages(null)
    }
}

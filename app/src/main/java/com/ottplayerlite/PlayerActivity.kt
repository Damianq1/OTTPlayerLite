package com.ottplayerlite

import android.graphics.Color
import android.os.*
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView

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
        
        val renderersFactory = DefaultRenderersFactory(this)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)

        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")

        player = ExoPlayer.Builder(this, renderersFactory)
            .setMediaSourceFactory(DefaultMediaSourceFactory(this).setDataSourceFactory(dataSourceFactory))
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(channel.url))
                prepare()
                play()
            }
        playerView.player = player
        
        if (ModuleManager.isEnabled(this, "stats")) startStatsMonitor()
    }

    private fun startStatsMonitor() {
        handler.post(object : Runnable {
            override fun run() {
                val format = player?.videoFormat
                if (format != null) {
                    findViewById<View>(R.id.statsContainer)?.visibility = View.VISIBLE
                    findViewById<TextView>(R.id.txtResolution)?.text = "Res: ${format.width}x${format.height}"
                    findViewById<TextView>(R.id.txtFps)?.text = "FPS: ${format.frameRate.toInt()}"
                }
                handler.postDelayed(this, 2000)
            }
        })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) { playChannel((currentIndex + 1) % playlist.size); return true }
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) { playChannel((currentIndex - 1 + playlist.size) % playlist.size); return true }
        if (keyCode == KeyEvent.KEYCODE_BACK) { finish(); return true }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        handler.removeCallbacksAndMessages(null)
    }
}

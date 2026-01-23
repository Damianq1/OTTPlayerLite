package com.ottplayerlite

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import java.net.InetSocketAddress
import java.net.Proxy

class PlayerActivity : AppCompatActivity() {
    companion object { var playlist: List<Channel> = listOf() }
    private var player: ExoPlayer? = null
    private var currentIndex = 0
    private val prefs by lazy { getSharedPreferences("OTT_DATA", Context.MODE_PRIVATE) }
    private val powerHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        
        val url = intent.getStringExtra("url") ?: ""
        currentIndex = playlist.indexOfFirst { it.url == url }.coerceAtLeast(0)
        
        playCurrent()
    }

    private fun playCurrent() {
        findViewById<ProgressBar>(R.id.loadingSpinner).visibility = View.VISIBLE
        player?.release()
        
        val channel = playlist[currentIndex]
        prefs.edit().putString("last_channel_url", channel.url).apply()

        player = ExoPlayer.Builder(this).build()
        val playerView = findViewById<androidx.media3.ui.PlayerView>(R.id.playerView)
        playerView.player = player
        
        player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    findViewById<ProgressBar>(R.id.loadingSpinner).visibility = View.GONE
                }
            }
        })

        player?.setMediaItem(MediaItem.fromUri(channel.url))
        player?.prepare()
        player?.play()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> { 
                currentIndex = if (currentIndex > 0) currentIndex - 1 else playlist.size - 1
                playCurrent()
                return true 
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> { 
                currentIndex = (currentIndex + 1) % playlist.size
                playCurrent()
                return true 
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }
}

package com.ottplayerlite

import android.app.PictureInPictureParams
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.KeyEvent
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.ottplayerlite.utils.AspectRatioManager
import com.ottplayerlite.utils.AFRManager

class PlayerActivity : AppCompatActivity() {
    companion object { var playlist: List<Channel> = listOf() }
    
    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private var currentIndex = 0
    private val prefs by lazy { getSharedPreferences("ULTIMATE_PREFS", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        AFRManager.setHighRefreshRate(this, window)
        playerView = findViewById(R.id.playerView)
        val url = intent.getStringExtra("url") ?: ""
        currentIndex = playlist.indexOfFirst { it.url == url }.coerceAtLeast(0)
        playCurrent()
    }

    private fun playCurrent() {
        val channel = playlist[currentIndex]
        findViewById<ProgressBar>(R.id.loadingSpinner).visibility = View.VISIBLE
        player?.release()

        val savedBuffer = prefs.getInt("buffer_${channel.url.hashCode()}", 3000)
        
        // POPRAWKA: Media3 DefaultLoadControl używa setBufferMs w Builderze inaczej
        val loadControl = DefaultLoadControl.Builder()
            .setBufferMs(savedBuffer, savedBuffer + 5000, 1000, 1500)
            .build()

        player = ExoPlayer.Builder(this)
            .setLoadControl(loadControl)
            .build()
            
        playerView.player = player
        player?.setMediaItem(MediaItem.fromUri(channel.url))
        player?.prepare()
        player?.play()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_PROG_GREEN -> {
                enterPipMode()
                return true
            }
            KeyEvent.KEYCODE_PROG_RED -> {
                val currentUrl = playlist[currentIndex].url
                val currentBuf = prefs.getInt("buffer_${currentUrl.hashCode()}", 3000)
                val newBuf = currentBuf + 2000
                prefs.edit().putInt("buffer_${currentUrl.hashCode()}", newBuf).apply()
                Toast.makeText(this, "Anti-Freeze: ${newBuf/1000}s", Toast.LENGTH_SHORT).show()
                playCurrent()
                return true
            }
            // KEYCODE_VIEW_MODE może nie być dostępny na wszystkich API, używamy Int
            82, KeyEvent.KEYCODE_PROG_YELLOW -> { 
                val nextMode = AspectRatioManager.getNextMode()
                playerView.resizeMode = nextMode
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            enterPictureInPictureMode(params)
        }
    }
}

package com.ottplayerlite

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.ottplayerlite.utils.UserAgentManager

class PlayerActivity : AppCompatActivity() {
    private var player: ExoPlayer? = null
    private var currentUrl: String = ""
    private var attempt = 0

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContentView(R.layout.activity_player)
        currentUrl = intent.getStringExtra("url") ?: ""
        startPlayback()
    }

    private fun startPlayback() {
        player?.release()
        
        // Wybieramy User-Agent w zależności od próby
        val ua = when(attempt) {
            0 -> UserAgentManager.getUserAgent(this) // Twoje ustawienie
            1 -> "VLC/3.0.16 LibVLC/3.0.16"        // Fallback na VLC
            else -> "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0" // Fallback na PC
        }

        val dsFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(ua)
            .setAllowCrossProtocolRedirects(true)

        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dsFactory))
            .build().apply {
                findViewById<androidx.media3.ui.PlayerView>(R.id.playerView).player = this
                setMediaItem(MediaItem.fromUri(Uri.parse(currentUrl)))
                prepare()
                playWhenReady = true
                
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        if (attempt < 2) {
                            attempt++
                            Logger.log("Błąd odtwarzania. Próba ${attempt} z nowym User-Agentem...")
                            startPlayback()
                        } else {
                            Toast.makeText(this@PlayerActivity, "Serwer odrzuca połączenie.", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                })
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }
}

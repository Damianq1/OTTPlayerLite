package com.ottplayerlite

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.ottplayerlite.utils.UserAgentManager
import com.ottplayerlite.utils.Logger

class PlayerActivity : AppCompatActivity() {
    private var player: ExoPlayer? = null
    private var currentUrl: String = ""
    private var attempt = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        currentUrl = intent.getStringExtra("url") ?: ""
        startPlayback()
    }

    private fun startPlayback() {
        player?.release()

        val ua = when(attempt) {
            0 -> UserAgentManager.getUserAgent(this)
            1 -> "VLC/3.0.18 LibVLC/3.0.18"
            else -> "Mozilla/5.0 (Linux; Android 10; TV) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36"
        }

        val dsFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(ua)
            .setAllowCrossProtocolRedirects(true)

        // Budujemy MediaItem z wymuszeniem typu HLS dla m3u8
        val mediaItem = MediaItem.Builder()
            .setUri(currentUrl)
            .setMimeType(if (currentUrl.contains("m3u8")) MimeTypes.APPLICATION_M3U8 else null)
            .build()

        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dsFactory))
            .build().apply {
                findViewById<androidx.media3.ui.PlayerView>(R.id.playerView).player = this
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true

                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        Logger.log("Błąd odtwarzania: ${error.message}")
                        if (attempt < 2) {
                            attempt++
                            startPlayback()
                        } else {
                            Toast.makeText(this@PlayerActivity, "Błąd strumienia. Spróbuj zmienić User-Agent w ustawieniach.", Toast.LENGTH_LONG).show()
                            finish()
                        }
                    }
                })
            }
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }
}

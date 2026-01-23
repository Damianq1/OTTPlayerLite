package com.ottplayerlite

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.DecoderCounters

@OptIn(UnstableApi::class)
class PlayerActivity : AppCompatActivity() {
    private var exoPlayer: ExoPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isSwitching = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        startExo(intent.getStringExtra("url") ?: "")
    }

    private fun startExo(url: String) {
        exoPlayer = ExoPlayer.Builder(this).build()
        findViewById<androidx.media3.ui.PlayerView>(R.id.playerView).player = exoPlayer
        
        exoPlayer?.addListener(object : Player.Listener {
            // WYKRYWANIE PROBLEMÓW Z DEKODEREM
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                // Jeśli obraz ma dziwne proporcje (np. 0x0), coś jest nie tak
                if (videoSize.width == 0 || videoSize.height == 0) {
                    switchToVlc(url, "Błąd rozmiaru wideo")
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                switchToVlc(url, "Błąd dekodowania HW")
            }
        })

        // MONITOROWANIE FRAME DROPÓW (Strażnik jakości)
        handler.postDelayed(object : Runnable {
            override fun run() {
                val counters: DecoderCounters? = exoPlayer?.videoDecoderCounters
                if (counters != null && counters.droppedBufferCount > 20) {
                    // Jeśli zgubiliśmy za dużo klatek w krótkim czasie - obraz rwie/są artefakty
                    switchToVlc(url, "Wykryto spadek jakości obrazu")
                } else {
                    handler.postDelayed(this, 2000)
                }
            }
        }, 2000)

        // ... reszta logiki ładowania ...
    }

    private fun switchToVlc(url: String, reason: String) {
        if (isSwitching) return
        isSwitching = true
        runOnUiThread {
            android.widget.Toast.makeText(this, reason + " -> Przełączam na VLC (Software)", android.widget.Toast.LENGTH_SHORT).show()
            exoPlayer?.release()
            // Tu logika startu VLC z opcją "--avcodec-hw=none" (Software Mode)
        }
    }
}

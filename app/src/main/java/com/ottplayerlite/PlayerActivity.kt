package com.ottplayerlite

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
class PlayerActivity : AppCompatActivity() {
    private var player: ExoPlayer? = null
    companion object { var playlist: List<Channel> = listOf() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // WYMUSZENIE PEŁNEGO EKRANU I LANDSCAPE
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setContentView(R.layout.activity_player)

        val playerView = findViewById<PlayerView>(R.id.playerView)
        val streamUrl = intent.getStringExtra("url") ?: return

        player = ExoPlayer.Builder(this)
            .setSeekBackIncrementMs(10000)
            .setSeekForwardIncrementMs(10000)
            .build()
            
        playerView.player = player
        playerView.keepScreenOn = true

        val mediaItem = MediaItem.fromUri(streamUrl)
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.play()

        // AUTO-RETRY I OBSŁUGA BŁĘDÓW (PRZEŁĄCZANIE)
        player?.addListener(object : Player.Listener {
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                // Tu można dodać logikę przełączania na link zapasowy
                player?.prepare() // Próba ponownego załadowania
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }
}

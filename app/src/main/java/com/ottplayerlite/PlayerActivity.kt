package com.ottplayerlite

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.util.VLCVideoLayout

@OptIn(UnstableApi::class)
class PlayerActivity : AppCompatActivity() {
    private var exoPlayer: ExoPlayer? = null
    private var libVLC: LibVLC? = null
    private var vlcPlayer: org.videolan.libvlc.MediaPlayer? = null
    
    private val timeoutHandler = Handler(Looper.getMainLooper())
    private var isVlcActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setContentView(R.layout.activity_player)

        val streamUrl = intent.getStringExtra("url") ?: return
        
        // KROK 1: ZACZYNAMY OD EXOPLAYERA
        startExo(streamUrl)

        // KROK 2: USTAWAMY TIMER NA 2 SEKUNDY
        timeoutHandler.postDelayed({
            if (exoPlayer?.playbackState != Player.STATE_READY && !isVlcActive) {
                switchToVLC(streamUrl)
            }
        }, 2000)
    }

    private fun startExo(url: String) {
        val playerView = findViewById<androidx.media3.ui.PlayerView>(R.id.playerView)
        playerView.visibility = View.VISIBLE
        
        exoPlayer = ExoPlayer.Builder(this).build()
        playerView.player = exoPlayer
        
        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    timeoutHandler.removeCallbacksAndMessages(null) // Sukces! Anuluj przełączanie
                }
            }
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                switchToVLC(url) // Błąd natychmiastowy -> przełącz na VLC
            }
        })

        exoPlayer?.setMediaItem(MediaItem.fromUri(url))
        exoPlayer?.prepare()
        exoPlayer?.play()
    }

    private fun switchToVLC(url: String) {
        if (isVlcActive) return
        isVlcActive = true
        
        runOnUiThread {
            Toast.makeText(this, "ExoPlayer zawiódł, uruchamiam VLC...", Toast.LENGTH_SHORT).show()
            
            // Zamknij ExoPlayera
            exoPlayer?.stop()
            exoPlayer?.release()
            exoPlayer = null
            findViewById<View>(R.id.playerView).visibility = View.GONE

            // Uruchom VLC
            val vlcLayout = findViewById<VLCVideoLayout>(R.id.vlcLayout)
            vlcLayout.visibility = View.VISIBLE
            
            libVLC = LibVLC(this, arrayListOf("-vvv"))
            vlcPlayer = org.videolan.libvlc.MediaPlayer(libVLC)
            vlcPlayer?.attachViews(vlcLayout, null, false, false)
            
            val media = Media(libVLC, Uri.parse(url))
            vlcPlayer?.media = media
            vlcPlayer?.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timeoutHandler.removeCallbacksAndMessages(null)
        exoPlayer?.release()
        vlcPlayer?.release()
        libVLC?.release()
    }
}

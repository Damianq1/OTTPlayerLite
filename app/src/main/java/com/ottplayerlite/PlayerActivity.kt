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
        
        // WYMUSZENIE 120Hz/MAX REFRESH RATE
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

        // ANTI-FREEZE: Pobieranie zapamiętanego bufora dla tego konkretnego URL
        // Domyślnie 3 sekundy, jeśli kanał sprawiał problemy, może być więcej
        val savedBuffer = prefs.getInt("buffer_${channel.url.hashCode()}", 3000)
        
        val loadControl = DefaultLoadControl.Builder()
            .setBufferMs(savedBuffer, savedBuffer + 2000, 1000, 1500)
            .setPrioritizeTimeOverSizeThresholds(true)
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
            // ZIELONY PRZYCISK: Picture-in-Picture (Dedykowany)
            KeyEvent.KEYCODE_PROG_GREEN -> {
                enterPipMode()
                return true
            }
            
            // CZERWONY PRZYCISK: Zwiększ Anti-Freeze (Bufor) dla tego kanału
            KeyEvent.KEYCODE_PROG_RED -> {
                val currentUrl = playlist[currentIndex].url
                val currentBuf = prefs.getInt("buffer_${currentUrl.hashCode()}", 3000)
                val newBuf = currentBuf + 2000 // Dodaj 2 sekundy
                prefs.edit().putInt("buffer_${currentUrl.hashCode()}", newBuf).apply()
                
                Toast.makeText(this, "Anti-Freeze: zwiększono bufor do ${newBuf/1000}s", Toast.LENGTH_SHORT).show()
                playCurrent() // Restart z nowym buforem
                return true
            }

            KeyEvent.KEYCODE_PROG_YELLOW, KeyEvent.KEYCODE_VIEW_MODE -> {
                val nextMode = AspectRatioManager.getNextMode()
                playerView.resizeMode = nextMode
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    /# Fragment logiki w PlayerActivity do obsługi archiwum
    
    
    fun playArchive(baseUrl: String, startTime: String, duration: Int) {
    //     Formatowanie URL dla Xtream/M3U (dodanie timestampu)
            val archiveUrl ="\$baseUrl?utc=\$startTime&lutc=\$startTime" 
        // lunc/utc to standardowe parametry dla catchupu
            >playUrl(archiveUrl)
}

    private fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            enterPictureInPictureMode(params)
        }
    }

    override fun onUserLeaveHint() {
        // Opcjonalnie: PiP przy wyjściu do Home
        super.onUserLeaveHint()
    }
}

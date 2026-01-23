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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.*
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.util.VLCVideoLayout
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

@OptIn(UnstableApi::class)
class PlayerActivity : AppCompatActivity() {

    companion object {
        @JvmStatic
        var playlist: List<Channel> = listOf()
    }

    private var exoPlayer: ExoPlayer? = null
    private var libVLC: LibVLC? = null
    private var vlcPlayer: org.videolan.libvlc.MediaPlayer? = null
    private var currentIndex = 0
    private var isVlcActive = false
    private var currentProgram: EpgProgram? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        @Suppress("DEPRECATION")
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setContentView(R.layout.activity_player)

        val url = intent.getStringExtra("url") ?: ""
        currentIndex = playlist.indexOfFirst { it.url == url }.coerceAtLeast(0)

        setupUI()
        startPlayback(url)
        startClock()
    }

    private fun setupUI() {
        val panel = findViewById<View>(R.id.controlsPanel)
        val toggle = View.OnClickListener {
            panel.visibility = if (panel.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        findViewById<View>(R.id.playerView).setOnClickListener(toggle)
        findViewById<View>(R.id.vlcLayout).setOnClickListener(toggle)

        findViewById<Button>(R.id.btnNext).setOnClickListener { playNext() }
        findViewById<Button>(R.id.btnPrev).setOnClickListener { playPrev() }
        
        findViewById<TextView>(R.id.txtProgramDesc).setOnClickListener {
            currentProgram?.let { prog ->
                AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                    .setTitle(prog.title)
                    .setMessage(prog.description)
                    .setPositiveButton("Zamknij", null)
                    .show()
            }
        }
    }

    private fun startPlayback(url: String) {
        isVlcActive = false
        findViewById<View>(R.id.playerView).visibility = View.VISIBLE
        findViewById<View>(R.id.vlcLayout).visibility = View.GONE
        
        exoPlayer = ExoPlayer.Builder(this).build()
        findViewById<androidx.media3.ui.PlayerView>(R.id.playerView).player = exoPlayer
        
        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) { switchToVLC(url) }
        })

        exoPlayer?.setMediaItem(MediaItem.fromUri(url))
        exoPlayer?.prepare()
        exoPlayer?.play()

        handler.postDelayed({
            if (exoPlayer?.playbackState != Player.STATE_READY && !isVlcActive) switchToVLC(url)
        }, 2500)
        
        loadEpg()
    }

    private fun switchToVLC(url: String) {
        if (isVlcActive) return
        isVlcActive = true
        runOnUiThread {
            exoPlayer?.release()
            findViewById<View>(R.id.playerView).visibility = View.GONE
            val vv = findViewById<VLCVideoLayout>(R.id.vlcLayout)
            vv.visibility = View.VISIBLE
            
            // NAPRAWA ARTEFAKTÓW: Wyłączamy akcelerację sprzętową (Software Mode)
            val options = arrayListOf("--avcodec-hw=none", "--network-caching=3000")
            libVLC = LibVLC(this, options)
            vlcPlayer = org.videolan.libvlc.MediaPlayer(libVLC)
            vlcPlayer?.attachViews(vv, null, false, false)
            vlcPlayer?.media = Media(libVLC, Uri.parse(url))
            vlcPlayer?.play()
        }
    }

    private fun loadEpg() {
        val channel = playlist[currentIndex]
        findViewById<TextView>(R.id.currentChannelTitle).text = channel.name
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val conn = URL("https://epg.ovh/plar.gz").openConnection()
                val programs = EpgParser.parseGz(conn.getInputStream(), channel.name)
                val now = System.currentTimeMillis()
                currentProgram = programs.find { now in it.start..it.stop }
                withContext(Dispatchers.Main) {
                    currentProgram?.let {
                        findViewById<TextView>(R.id.txtProgramDesc).text = "${it.title}\n\n${it.description}"
                        findViewById<ProgressBar>(R.id.epgProgress).progress = ((now - it.start) * 100 / (it.stop - it.start)).toInt()
                    }
                }
            } catch (e: Exception) {}
        }
    }

    private fun playNext() { currentIndex = (currentIndex + 1) % playlist.size; refresh() }
    private fun playPrev() { currentIndex = (currentIndex - 1 + playlist.size) % playlist.size; refresh() }
    private fun refresh() { exoPlayer?.release(); vlcPlayer?.release(); startPlayback(playlist[currentIndex].url) }
    
    private fun startClock() {
        handler.post(object : Runnable {
            override fun run() {
                findViewById<TextView>(R.id.txtTime).text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                handler.postDelayed(this, 30000)
            }
        })
    }

    override fun onDestroy() { super.onDestroy(); exoPlayer?.release(); vlcPlayer?.release() }
}

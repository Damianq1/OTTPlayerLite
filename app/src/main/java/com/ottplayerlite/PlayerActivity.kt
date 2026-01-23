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
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.*
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.util.VLCVideoLayout
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(UnstableApi::class)
class PlayerActivity : AppCompatActivity() {
    private var exoPlayer: ExoPlayer? = null
    private var libVLC: LibVLC? = null
    private var vlcPlayer: org.videolan.libvlc.MediaPlayer? = null
    
    private var currentIndex = 0
    private var isVlcActive = false
    private var currentProgram: EpgProgram? = null
    private val handler = Handler(Looper.getMainLooper())

    companion object { var playlist: List<Channel> = listOf() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        val clickListener = View.OnClickListener {
            panel.visibility = if (panel.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        findViewById<View>(R.id.playerView).setOnClickListener(clickListener)
        findViewById<View>(R.id.vlcLayout).setOnClickListener(clickListener)

        findViewById<Button>(R.id.btnNext).setOnClickListener { playNext() }
        findViewById<Button>(R.id.btnPrev).setOnClickListener { playPrev() }
        
        findViewById<Button>(R.id.btnSchedule).setOnClickListener {
            currentProgram?.let { prog -> scheduleRecording(prog, playlist[currentIndex]) }
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

        // Auto-switch po 2 sekundach
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
            
            libVLC = LibVLC(this)
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
                        val progress = ((now - it.start) * 100 / (it.stop - it.start)).toInt()
                        findViewById<ProgressBar>(R.id.epgProgress).progress = progress
                        findViewById<Button>(R.id.btnSchedule).visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { findViewById<TextView>(R.id.txtProgramDesc).text = "Brak danych EPG" }
            }
        }
    }

    private fun scheduleRecording(prog: EpgProgram, channel: Channel) {
        val delay = prog.start - System.currentTimeMillis()
        if (delay < 0) {
            Toast.makeText(this, "Program już się zaczął!", Toast.LENGTH_SHORT).show()
            return
        }

        val data = Data.Builder()
            .putString("url", channel.url)
            .putString("name", "PVR_${channel.name}_${prog.title}")
            .putLong("duration", prog.stop - prog.start)
            .build()

        val request = OneTimeWorkRequestBuilder<RecordingWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(this).enqueue(request)
        Toast.makeText(this, "Zaplanowano: ${prog.title}", Toast.LENGTH_SHORT).show()
    }

    private fun playNext() {
        currentIndex = (currentIndex + 1) % playlist.size
        refresh()
    }

    private fun playPrev() {
        currentIndex = (currentIndex - 1 + playlist.size) % playlist.size
        refresh()
    }

    private fun refresh() {
        exoPlayer?.release()
        vlcPlayer?.release()
        startPlayback(playlist[currentIndex].url)
    }

    private fun startClock() {
        handler.post(object : Runnable {
            override fun run() {
                findViewById<TextView>(R.id.txtTime).text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                handler.postDelayed(this, 30000)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        vlcPlayer?.release()
        handler.removeCallbacksAndMessages(null)
    }
}

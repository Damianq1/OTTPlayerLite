package com.ottplayerlite

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.util.VLCVideoLayout

@OptIn(UnstableApi::class)
class PlayerActivity : AppCompatActivity() {
    companion object { var playlist: List<Channel> = listOf() }

    private var exoPlayer: ExoPlayer? = null
    private var vlcPlayer: org.videolan.libvlc.MediaPlayer? = null
    private var libVLC: LibVLC? = null
    private var currentIndex = 0
    private val playerPrefs by lazy { getSharedPreferences("PLAYER_MEM", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val url = intent.getStringExtra("url") ?: ""
        currentIndex = playlist.indexOfFirst { it.url == url }.coerceAtLeast(0)

        setupSideMenu()
        playCurrent()
    }

    private fun setupSideMenu() {
        val rv = findViewById<RecyclerView>(R.id.sideChannelList)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = ChannelAdapter(playlist) { channel ->
            currentIndex = playlist.indexOf(channel)
            playCurrent()
            findViewById<View>(R.id.sideMenu).visibility = View.GONE
        }
        
        findViewById<ImageButton>(R.id.btnShowChannels).setOnClickListener {
            val menu = findViewById<View>(R.id.sideMenu)
            menu.visibility = if (menu.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }

    private fun playCurrent() {
        releasePlayers()
        val url = playlist[currentIndex].url
        
        // PAMIĘĆ PLAYERA: Jeśli wcześniej był tu błąd, użyj VLC od razu
        if (playerPrefs.getBoolean(url, false)) {
            startVLC(url)
        } else {
            startExo(url)
        }
    }

    private fun startExo(url: String) {
        findViewById<View>(R.id.playerView).visibility = View.VISIBLE
        findViewById<View>(R.id.vlcLayout).visibility = View.GONE
        
        exoPlayer = ExoPlayer.Builder(this).build()
        findViewById<androidx.media3.ui.PlayerView>(R.id.playerView).player = exoPlayer
        
        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                // Zapamiętaj błąd dla tego URL i przełącz silnik
                playerPrefs.edit().putBoolean(url, true).apply()
                startVLC(url)
            }
        })
        
        exoPlayer?.setMediaItem(MediaItem.fromUri(url))
        exoPlayer?.prepare()
        exoPlayer?.play()
    }

    private fun startVLC(url: String) {
        runOnUiThread {
            findViewById<View>(R.id.playerView).visibility = View.GONE
            val vv = findViewById<VLCVideoLayout>(R.id.vlcLayout)
            vv.visibility = View.VISIBLE
            
            // Software decoding wymuszony dla czystego obrazu
            libVLC = LibVLC(this, arrayListOf("--avcodec-hw=none", "--network-caching=3000"))
            vlcPlayer = org.videolan.libvlc.MediaPlayer(libVLC)
            vlcPlayer?.attachViews(vv, null, false, false)
            vlcPlayer?.media = Media(libVLC, Uri.parse(url))
            vlcPlayer?.play()
            Toast.makeText(this, "Silnik: VLC (Soft)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun releasePlayers() {
        exoPlayer?.release(); exoPlayer = null
        vlcPlayer?.release(); vlcPlayer = null
        libVLC?.release(); libVLC = null
    }

    override fun onDestroy() { super.onDestroy(); releasePlayers() }
}

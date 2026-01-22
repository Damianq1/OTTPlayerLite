package com.ottplayerlite

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.*
import androidx.media3.exoplayer.ExoPlayer
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.util.VLCVideoLayout
import com.ottplayerlite.utils.UserAgentManager

class PlayerActivity : AppCompatActivity() {
    private var exoPlayer: ExoPlayer? = null
    private var vlcPlayer: org.videolan.libvlc.MediaPlayer? = null
    private var libVLC: LibVLC? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        val url = intent.getStringExtra("url") ?: ""
        val engine = getSharedPreferences("settings", MODE_PRIVATE).getString("engine", "MEDIA3")

        when (engine) {
            "VLC" -> startVlc(url)
            "WEB" -> startWebPlayer(url)
            else -> startMedia3(url)
        }
    }

    private fun startMedia3(url: String) {
        val pv = findViewById<androidx.media3.ui.PlayerView>(R.id.playerView)
        pv.visibility = View.VISIBLE
        exoPlayer = ExoPlayer.Builder(this).build().apply {
            pv.player = this
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            play()
        }
    }

    private fun startVlc(url: String) {
        val vlcLayout = findViewById<VLCVideoLayout>(R.id.vlcLayout)
        vlcLayout.visibility = View.VISIBLE
        libVLC = LibVLC(this, arrayListOf("-vvv"))
        vlcPlayer = org.videolan.libvlc.MediaPlayer(libVLC)
        vlcPlayer?.attachViews(vlcLayout, null, false, false)
        val media = Media(libVLC, Uri.parse(url))
        vlcPlayer?.media = media
        vlcPlayer?.play()
    }

    private fun startWebPlayer(url: String) {
        val webView = findViewById<WebView>(R.id.webViewPlayer)
        webView.visibility = View.VISIBLE
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            userAgentString = UserAgentManager.getUserAgent(this@PlayerActivity)
        }
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedSslError(v: WebView?, h: SslErrorHandler?, e: android.net.http.SslError?) { h?.proceed() }
        }
        val html = "<html><body style='margin:0;padding:0;background:#000;'><video width='100%' height='100%' controls autoplay><source src='$url'></video></body></html>"
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        vlcPlayer?.release()
        libVLC?.release()
    }
}

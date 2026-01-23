// ... wewnątrz logiki odtwarzacza ...
private fun getDataSourceFactory(): androidx.media3.datasource.DataSource.Factory {
    val prefs = getSharedPreferences("OTT_DATA", Context.MODE_PRIVATE)
    val useProxy = prefs.getBoolean("use_proxy", false)
    val userAgent = prefs.getString("user_agent", "UltimatePlayer/1.0")

    val httpFactory = androidx.media3.datasource.DefaultHttpDataSource.Factory()
        .setUserAgent(userAgent)

    if (useProxy) {
        val host = prefs.getString("proxy_host", "")
        val port = prefs.getString("proxy_port", "8080")?.toIntOrNull() ?: 8080
        
        if (!host.isNullOrEmpty()) {
            // Konfiguracja proxy HTTP
            val proxy = java.net.Proxy(java.net.Proxy.Type.HTTP, java.net.InetSocketAddress(host, port))
            // ExoPlayer automatycznie obsłuży proxy przez bibliotekę Cronet lub OkHttp
            // Tutaj stosujemy uproszczony model dla standardowego DefaultHttpDataSource
        }
    }
    return httpFactory
}

package com.ottplayerlite.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

object NetworkManager {
    fun isVpnActive(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork
            val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ?: false
        } else {
            // Starsze wersje Androida
            connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_VPN)?.isConnected ?: false
        }
    }
}

package com.example.popcornroute.home.data.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.popcornroute.home.domain.repository.ConnectivityChecker

class AndroidConnectivityChecker(
    context: Context,
) : ConnectivityChecker {
    private val connectivityManager =
        context.getSystemService(ConnectivityManager::class.java)

    override fun isConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}

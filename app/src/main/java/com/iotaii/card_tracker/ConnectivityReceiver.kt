package com.iotaii.card_tracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.widget.Toast

class ConnectivityReceiver : BroadcastReceiver() {

    companion object {
        private var isConnected = true
    }

    override fun onReceive(context: Context, intent: Intent) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val currentlyConnected = activeNetwork?.isConnectedOrConnecting == true

        if (currentlyConnected && !isConnected) {
            isConnected = true
            Toast.makeText(context, "Internet is turned on", Toast.LENGTH_SHORT).show()
        } else if (!currentlyConnected && isConnected) {
            isConnected = false
            Toast.makeText(context, "Please Turn On Your Internet!", Toast.LENGTH_SHORT).show()
        }
    }
}

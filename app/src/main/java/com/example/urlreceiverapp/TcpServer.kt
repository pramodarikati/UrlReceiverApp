package com.example.urlreceiverapp

import android.util.Log
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket

class TcpServer(private val onUrlReceived: (String) -> Unit) {

    private val port = 11007

    fun startServer() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val serverSocket = ServerSocket(port)
                Log.d("TcpServer", "Listening on port $port")
                while (true) {
                    val socket = serverSocket.accept()
                    val input = BufferedReader(InputStreamReader(socket.getInputStream()))
                    val url = input.readLine()
                    Log.d("TcpServer", "Received: $url")
                    if (!url.isNullOrBlank()) {
                        withContext(Dispatchers.Main) {
                            onUrlReceived(url)
                        }
                    }
                    socket.close()
                }
            } catch (e: Exception) {
                Log.e("TcpServer", "Error: ${e.message}")
            }
        }
    }
}

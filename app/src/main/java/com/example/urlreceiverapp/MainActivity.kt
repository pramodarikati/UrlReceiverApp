package com.example.urlreceiverapp

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.util.concurrent.CopyOnWriteArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val imageUrls = CopyOnWriteArrayList<String>()
    private var htmlLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()

        startServer()
    }

    private fun startServer() {
        CoroutineScope(Dispatchers.IO).launch {
            val serverSocket = ServerSocket(11007)

            while (true) {
                val socket = serverSocket.accept()
                val input = BufferedReader(InputStreamReader(socket.getInputStream()))
                val url = input.readLine()
                socket.close()

                if (!url.isNullOrBlank()) {
                    imageUrls.add(url)
                    updateSlideshow()
                }
            }
        }
    }

    private fun updateSlideshow() {
        if (!htmlLoaded) {
            val htmlContent = """
                <html>
                <head>
                    <style>
                        body { margin: 0; background: black; display: flex; justify-content: center; align-items: center; height: 100vh; }
                        img { max-width: 100%; max-height: 100%; object-fit: contain; }
                    </style>
                </head>
                <body>
                    <img id="slideshow" src="" />
                    <script>
                        let images = [];
                        let index = 0;

                        function showNextImage() {
                            if (images.length > 0) {
                                document.getElementById("slideshow").src = images[index];
                                index = (index + 1) % images.length;
                            }
                        }

                        setInterval(showNextImage, 3000);
                    </script>
                </body>
                </html>
            """.trimIndent()

            runOnUiThread {
                webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
            }
            htmlLoaded = true

            CoroutineScope(Dispatchers.Main).launch {
                delay(1000)
                injectUrlsToWebView()
            }
        } else {
            injectUrlsToWebView()
        }
    }

    private fun injectUrlsToWebView() {
        val jsArray = imageUrls.joinToString(",") { "\"$it\"" }
        val jsScript = "javascript:images = [$jsArray];"
        runOnUiThread {
            webView.evaluateJavascript(jsScript, null)
        }
    }
}

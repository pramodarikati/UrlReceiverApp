package com.example.urlreceiverapp;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

import android.os.Handler;
import android.os.Looper;

public class MainActivity extends AppCompatActivity {


    private WebView webView;
    private final CopyOnWriteArrayList<String> imageUrls = new CopyOnWriteArrayList<>();
    private boolean htmlLoaded = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        startServer();
    }

    private void startServer() {
        Thread serverThread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(11007)) {
                while (true) {
                    Socket socket = serverSocket.accept();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String url = reader.readLine();
                    socket.close();

                    if (url != null && !url.trim().isEmpty()) {
                        imageUrls.add(url.trim());
                        updateSlideshow();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        serverThread.start();
    }

    private void updateSlideshow() {
        if (!htmlLoaded) {
            final String htmlContent = "<html>\n" +
                    "<head>\n" +
                    "<style>\n" +
                    "body { margin: 0; background: black; display: flex; justify-content: center; align-items: center; height: 100vh; }\n" +
                    "img { max-width: 100%; max-height: 100%; object-fit: contain; }\n" +
                    "</style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<img id=\"slideshow\" src=\"\" />\n" +
                    "<script>\n" +
                    "let images = [];\n" +
                    "let index = 0;\n" +
                    "function showNextImage() {\n" +
                    "  if (images.length > 0) {\n" +
                    "    document.getElementById(\"slideshow\").src = images[index];\n" +
                    "    index = (index + 1) % images.length;\n" +
                    "  }\n" +
                    "}\n" +
                    "setInterval(showNextImage, 3000);\n" +
                    "</script>\n" +
                    "</body>\n" +
                    "</html>";

            mainHandler.post(() -> {
                webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
            });

            htmlLoaded = true;

            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    injectUrlsToWebView();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } else {
            injectUrlsToWebView();
        }
    }

    private void injectUrlsToWebView() {
        StringBuilder jsArray = new StringBuilder();
        for (int i = 0; i < imageUrls.size(); i++) {
            jsArray.append("\"").append(imageUrls.get(i)).append("\"");
            if (i < imageUrls.size() - 1) {
                jsArray.append(",");
            }
        }

        final String jsScript = "javascript:images = [" + jsArray.toString() + "];";

        mainHandler.post(() -> {
            webView.evaluateJavascript(jsScript, null);
        });
    }
}

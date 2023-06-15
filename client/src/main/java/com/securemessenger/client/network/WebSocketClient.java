package com.securemessenger.client.network;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;


public class WebSocketClient {
    private OkHttpClient client;
    private WebSocket webSocket;
    private WebSocketListener listener;

    public WebSocketClient() {
        client = new OkHttpClient();
    }

    public void setListener(WebSocketListener listener) {
        this.listener = listener;
    }

    public void connect(String url) {
        Request request = new Request.Builder().url(url).build();
        webSocket = client.newWebSocket(request, listener);
    }

    public void sendMessage(String message) {
        if (webSocket != null) {
            webSocket.send(message);
        }
    }

    public void close() {
        if (webSocket != null) {
            webSocket.close(1000, "Closing WebSocket connection");
        }
    }
}
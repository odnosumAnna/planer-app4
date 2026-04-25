package com.example.planer.data.socket

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.*

class SocketManager {

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    private var messageHandler: ((String) -> Unit)? = null
    private var currentUrl: String? = null

    // ✅ ЄДИНИЙ state
    private val _state = MutableStateFlow(SocketState.Disconnected)
    val state: StateFlow<SocketState> = _state

    // 🔹 CONNECT
    fun connect(url: String) {
        currentUrl = url
        _state.value = SocketState.Connecting

        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(ws: WebSocket, response: Response) {
                _state.value = SocketState.Connected
            }

            override fun onMessage(ws: WebSocket, text: String) {
                messageHandler?.invoke(text)
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                _state.value = SocketState.Reconnecting
                reconnect()
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                _state.value = SocketState.Disconnected
            }
        })
    }

    // 🔹 RECONNECT
    private fun reconnect() {
        val url = currentUrl ?: return

        CoroutineScope(Dispatchers.IO).launch {
            delay(2000)
            connect(url)
        }
    }

    // 🔹 DISCONNECT
    fun disconnect() {
        webSocket?.close(1000, "Closed")
        webSocket = null
        _state.value = SocketState.Disconnected
    }

    // 🔹 SEND
    fun send(message: String) {
        webSocket?.send(message)
    }

    // 🔹 LISTENER
    fun onMessage(handler: (String) -> Unit) {
        messageHandler = handler
    }
}
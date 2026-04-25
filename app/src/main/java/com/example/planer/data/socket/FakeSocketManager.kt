package com.example.planer.data.socket
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FakeSocketManager {

    private var handler: ((String) -> Unit)? = null

    fun onMessage(h: (String) -> Unit) {
        handler = h
    }

    fun start() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(4000)

                val json = """{
                    "id": "ws_${System.currentTimeMillis()}",
                    "title": "Fake WS Task",
                    "description": "Generated automatically",
                    "isCompleted": false
                }"""

                handler?.invoke(json)
            }
        }
    }
}
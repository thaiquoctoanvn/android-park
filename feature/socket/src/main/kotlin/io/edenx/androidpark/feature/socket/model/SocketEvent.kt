package io.edenx.androidpark.feature.socket.model

/**
 * Raw socket events, before any parsing. A failure is not an event: it ends
 * the flow with an exception so retry operators can see it.
 */
sealed interface SocketEvent {
    data object Opened : SocketEvent
    data class Text(val text: String) : SocketEvent
    data class Closed(val code: Int, val reason: String) : SocketEvent
}

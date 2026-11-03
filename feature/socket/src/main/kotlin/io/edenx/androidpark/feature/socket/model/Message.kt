package io.edenx.androidpark.feature.socket.model

/** What the server pushes to the client. */
sealed interface ServerMessage {
    /**
     * [seq] grows by one per message on a connection, so a jump means frames
     * were lost.
     */
    data class Tick(val seq: Long, val symbol: String, val price: Double, val ts: Long) : ServerMessage

    /** Confirms a [Command] by its id, so the client can drop it from the outbox. */
    data class Ack(val commandId: String) : ServerMessage
}

/**
 * What the client sends to the server. [id] lets the server ignore a command
 * it has already applied when the client resends after a reconnect.
 */
data class Command(val id: String, val type: Type, val symbols: List<String>) {
    enum class Type { SUBSCRIBE, UNSUBSCRIBE }
}

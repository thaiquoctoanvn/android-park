package io.edenx.androidpark.feature.socket.data

import io.edenx.androidpark.feature.socket.model.Command
import io.edenx.androidpark.feature.socket.model.ConnectionState
import io.edenx.androidpark.feature.socket.model.MessageCodec
import io.edenx.androidpark.feature.socket.model.ServerMessage
import io.edenx.androidpark.feature.socket.model.SocketEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.io.IOException
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.min
import kotlin.random.Random

/**
 * Turns a WebSocket into a cold [Flow] of ticks that survives disconnects.
 *
 * What keeps it cheap at thousands of frames per second:
 * - OkHttp's reader thread only hands raw text to a bounded buffer. When the
 *   buffer is full it blocks, so a slow consumer slows the TCP read instead of
 *   growing memory without limit.
 * - Decoding runs on [Dispatchers.Default], never on the reader thread or main.
 * - Nothing here touches the UI. Rate limiting for the screen is
 *   [coalesceLatest]'s job, downstream.
 */
class TickerClient(
    private val okHttp: OkHttpClient,
    private val endpoint: suspend () -> String,
    private val baseBackoffMs: Long = 500,
    private val maxBackoffMs: Long = 10_000,
    private val maxAttempts: Int = Int.MAX_VALUE,
    private val bufferCapacity: Int = 1_024,
) {
    private val _state = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    val state: StateFlow<ConnectionState> = _state.asStateFlow()

    /** Monotonic counters. Readers compute rates by diffing two snapshots. */
    val stats = Counters()

    class Counters {
        val frames = AtomicLong()
        val ticks = AtomicLong()
        val malformed = AtomicLong()
        /** Ticks the server sent but this client never saw, from holes in `seq`. */
        val lost = AtomicLong()
        val reconnects = AtomicLong()
    }

    /**
     * Connects on collect, subscribes to [symbols], and reconnects with
     * backoff on any I/O failure. Cancelling the collector closes the socket.
     * Only gives up, with [ConnectionState.Failed], after [maxAttempts]
     * failures in a row.
     */
    fun ticks(symbols: List<String>): Flow<ServerMessage.Tick> = flow {
        var attempt = 0
        while (true) {
            _state.value = ConnectionState.Connecting
            try {
                // seq restarts with each connection, so gap tracking does too.
                var lastSeq = -1L
                session(symbols).collect { event ->
                    when (event) {
                        SocketEvent.Opened -> {
                            attempt = 0
                            _state.value = ConnectionState.Connected
                        }
                        is SocketEvent.Text -> {
                            stats.frames.incrementAndGet()
                            when (val message = MessageCodec.decodeServerMessage(event.text)) {
                                is ServerMessage.Tick -> {
                                    if (lastSeq >= 0 && message.seq > lastSeq + 1) {
                                        stats.lost.addAndGet(message.seq - lastSeq - 1)
                                    }
                                    lastSeq = message.seq
                                    stats.ticks.incrementAndGet()
                                    emit(message)
                                }
                                is ServerMessage.Ack -> Unit
                                null -> stats.malformed.incrementAndGet()
                            }
                        }
                        is SocketEvent.Closed -> throw ServerClosedException(event.code, event.reason)
                    }
                }
                throw ServerClosedException(code = -1, reason = "stream ended")
            } catch (e: IOException) {
                attempt++
                if (attempt > maxAttempts) {
                    _state.value = ConnectionState.Failed(e)
                    throw e
                }
                val wait = backoff(attempt)
                _state.value = ConnectionState.Retrying(attempt, wait)
                stats.reconnects.incrementAndGet()
                delay(wait)
            }
        }
    }
        .flowOn(Dispatchers.Default)
        // After flowOn, so it runs in the collector's coroutine: the state is
        // Idle by the time collect returns, not some time later.
        .onCompletion { cause ->
            if (cause == null || cause is CancellationException) _state.value = ConnectionState.Idle
        }

    /** One connection, as raw events. Fails with the socket's exception. */
    private fun session(symbols: List<String>): Flow<SocketEvent> = callbackFlow {
        val request = Request.Builder().url(endpoint()).build()
        val socket = okHttp.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                // Sent on every open, so a reconnect restores the subscription.
                webSocket.send(MessageCodec.encode(Command(UUID.randomUUID().toString(), Command.Type.SUBSCRIBE, symbols)))
                trySendBlocking(SocketEvent.Opened)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                // Blocks OkHttp's reader thread while the buffer is full: this is
                // the backpressure. Fails silently once the flow is cancelled.
                trySendBlocking(SocketEvent.Text(text))
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(NORMAL_CLOSURE, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                trySendBlocking(SocketEvent.Closed(code, reason))
                channel.close()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                channel.close(t as? IOException ?: IOException(t))
            }
        })
        // cancel() not close(): a close handshake would wait for frames we no longer want.
        awaitClose { socket.cancel() }
    }.buffer(bufferCapacity)

    /** Exponential, capped, with jitter so many clients don't reconnect in lockstep. */
    private fun backoff(attempt: Int): Long {
        val exp = min(maxBackoffMs, baseBackoffMs shl min(attempt - 1, 20))
        return exp / 2 + Random.nextLong(exp / 2 + 1)
    }

    class ServerClosedException(code: Int, reason: String) : IOException("closed by server: $code $reason")

    private companion object {
        const val NORMAL_CLOSURE = 1000
    }
}

package io.edenx.androidpark.feature.socket.data

import io.edenx.androidpark.feature.socket.model.Command
import io.edenx.androidpark.feature.socket.model.MessageCodec
import io.edenx.androidpark.feature.socket.model.ServerMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

/**
 * An in-process WebSocket server that pushes price ticks as fast as
 * [ticksPerSecond] asks, so the client can be watched under real load without
 * a backend.
 *
 * Each tick is its own frame, the worst case for a client: no server-side
 * batching to hide behind.
 */
class FakeTickerServer(
    /** Share of ticks skipped on purpose, so the client's gap counter has something to find. */
    private val lossRate: Double = 0.0005,
) {
    /** Read by every push loop on each step, so a change applies without reconnecting. */
    @Volatile
    var ticksPerSecond: Int = 1_000

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val sessions = ConcurrentHashMap<WebSocket, Session>()
    private val startLock = Mutex()
    private var server: MockWebServer? = null
    private var url: String? = null

    /** Starts the server on first call. Binding a socket is I/O, so it never runs on the caller's thread. */
    suspend fun url(): String = startLock.withLock {
        url ?: withContext(Dispatchers.IO) {
            val started = MockWebServer().apply {
                dispatcher = object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest) =
                        MockResponse().withWebSocketUpgrade(listener)
                }
                start()
            }
            server = started
            // url() resolves the host name, which is also I/O.
            started.url("/ticker").toString()
        }.also { url = it }
    }

    /**
     * Closes every open connection from the server side, as a server restart
     * would. (Server-side sockets in MockWebServer have no Call, so cancel()
     * throws; a close frame is the only way to drop them.)
     */
    fun dropConnections() {
        sessions.values.forEach { it.job?.cancel() }
        sessions.keys.forEach { it.close(SERVICE_RESTART, "restarting") }
    }

    fun shutdown() {
        scope.cancel()
        dropConnections()
        server?.shutdown()
    }

    private val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            val session = Session(webSocket)
            sessions[webSocket] = session
            session.job = scope.launch { session.push() }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val command = MessageCodec.decodeCommand(text) ?: return
            sessions[webSocket]?.apply(command)
            webSocket.send(MessageCodec.encode(ServerMessage.Ack(command.id)))
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(code, null)
            sessions.remove(webSocket)?.job?.cancel()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            sessions.remove(webSocket)?.job?.cancel()
        }
    }

    private inner class Session(private val socket: WebSocket) {
        var job: Job? = null

        /** Copy-on-write: replaced whole by commands, read lock-free by the push loop. */
        @Volatile
        private var symbols: List<String> = emptyList()
        private val prices = HashMap<String, Double>()   // touched only by the push loop
        private var seq = 0L

        fun apply(command: Command) {
            symbols = when (command.type) {
                Command.Type.SUBSCRIBE -> (symbols + command.symbols).distinct()
                Command.Type.UNSUBSCRIBE -> symbols - command.symbols.toSet()
            }
        }

        suspend fun push() {
            var owed = 0.0
            while (scope.isActive) {
                delay(STEP_MS)
                val current = symbols
                if (current.isEmpty()) continue
                // Carry the fraction over so low rates (e.g. 50/s) still come out right.
                owed += ticksPerSecond * STEP_MS / 1000.0
                repeat(owed.toInt()) {
                    if (Random.nextDouble() < lossRate) seq++   // a "lost" frame
                    val symbol = current[Random.nextInt(current.size)]
                    val price = nextPrice(symbol)
                    val tick = ServerMessage.Tick(++seq, symbol, price, System.currentTimeMillis())
                    // false means OkHttp's 16 MB outgoing queue is full and it is
                    // closing the socket: the client is not keeping up.
                    if (!socket.send(MessageCodec.encode(tick))) return
                }
                owed -= owed.toInt()
            }
        }

        private fun nextPrice(symbol: String): Double {
            val last = prices[symbol] ?: (50.0 + Random.nextDouble() * 450.0)
            val next = (last * (1 + (Random.nextDouble() - 0.5) * 0.002)).coerceAtLeast(0.01)
            prices[symbol] = next
            return next
        }
    }

    private companion object {
        const val STEP_MS = 10L
        const val SERVICE_RESTART = 1012
    }
}

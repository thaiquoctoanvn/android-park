package io.edenx.androidpark.feature.socket.data

import io.edenx.androidpark.feature.socket.model.ConnectionState
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Runs against the real in-process server over a real socket, in real time. */
class TickerClientTest {

    private var server = FakeTickerServer(lossRate = 0.0).apply { ticksPerSecond = 5_000 }

    private fun client() = TickerClient(
        okHttp = OkHttpClient(),
        endpoint = server::url,
        baseBackoffMs = 20,
        maxBackoffMs = 50,
    )

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun receivesOnlySubscribedSymbols_inSeqOrder() = runBlocking {
        val client = client()
        val ticks = withTimeout(10_000) { client.ticks(listOf("AAPL", "GOOG")).take(500).toList() }

        assertTrue(ticks.all { it.symbol in setOf("AAPL", "GOOG") })
        assertEquals(ticks.map { it.seq }, (ticks.first().seq until ticks.first().seq + 500).toList())
        assertEquals(0L, client.stats.lost.get())
        assertEquals(ConnectionState.Idle, client.state.value)
    }

    @Test
    fun reconnectsAndResubscribes_afterTheConnectionDrops() = runBlocking {
        val client = client()
        var seen = 0
        val ticks = withTimeout(10_000) {
            client.ticks(listOf("AAPL"))
                .onEach { if (++seen == 100) server.dropConnections() }
                .take(400)
                .toList()
        }

        assertTrue(client.stats.reconnects.get() >= 1)
        // A new connection restarts seq, so the stream must step back at least once.
        assertTrue(ticks.zipWithNext().any { (a, b) -> b.seq < a.seq })
        assertTrue(ticks.all { it.symbol == "AAPL" })
    }

    @Test
    fun seqGaps_areCountedAsLost() = runBlocking {
        server.shutdown()
        server = FakeTickerServer(lossRate = 0.5).apply { ticksPerSecond = 5_000 }
        val client = client()
        withTimeout(10_000) { client.ticks(listOf("AAPL")).take(200).toList() }

        assertTrue(client.stats.lost.get() > 0)
    }
}

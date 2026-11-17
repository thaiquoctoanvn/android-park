package io.edenx.androidpark.feature.socket.data

import io.edenx.androidpark.feature.socket.model.ServerMessage.Tick
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class CoalesceLatestTest {

    private fun tick(seq: Long, symbol: String, price: Double) = Tick(seq, symbol, price, ts = seq)

    @Test
    fun keepsOnlyTheNewestTickPerSymbol_perWindow() = runTest {
        val batches = flow {
            emit(tick(1, "A", 1.0))
            emit(tick(2, "A", 2.0))
            emit(tick(3, "B", 3.0))
            delay(150)
            emit(tick(4, "A", 4.0))
        }.coalesceLatest(periodMs = 100).toList()

        assertEquals(
            listOf(
                mapOf("A" to tick(2, "A", 2.0), "B" to tick(3, "B", 3.0)),
                mapOf("A" to tick(4, "A", 4.0)),
            ),
            batches,
        )
    }

    @Test
    fun aSymbolThatTickedOnce_isNotLostBehindABusyOne() = runTest {
        val batches = flow {
            emit(tick(1, "QUIET", 1.0))
            repeat(1_000) { emit(tick(2L + it, "BUSY", it.toDouble())) }
        }.coalesceLatest(periodMs = 100).toList()

        assertEquals(1, batches.size)
        assertEquals(setOf("QUIET", "BUSY"), batches.single().keys)
        assertEquals(999.0, batches.single().getValue("BUSY").price, 0.0)
    }

    @Test
    fun emptyWindows_emitNothing() = runTest {
        val batches = flow {
            emit(tick(1, "A", 1.0))
            delay(1_000)
            emit(tick(2, "A", 2.0))
        }.coalesceLatest(periodMs = 100).toList()

        assertEquals(2, batches.size)
    }

    @Test
    fun upstreamFailure_propagates() = runTest {
        val failure = runCatching {
            flow<Tick> {
                emit(tick(1, "A", 1.0))
                throw IOException("boom")
            }.coalesceLatest(periodMs = 100).toList()
        }.exceptionOrNull()

        assertTrue("got $failure", failure is IOException)
    }
}

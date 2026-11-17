package io.edenx.androidpark.feature.socket.data

import io.edenx.androidpark.feature.socket.model.ServerMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

/**
 * Collapses a fast tick stream into at most one batch per [periodMs], holding
 * only the newest tick per symbol.
 *
 * The screen cannot show more than one price per symbol per frame, so every
 * tick that is overwritten before the next batch is work the UI never has to
 * do. At 10 000 ticks/s over 30 symbols and a 100 ms period, that is 1 000
 * ticks in and at most 30 rows changed out, 10 times a second.
 *
 * Unlike `conflate()` or `sample()`, nothing is lost per key: a symbol that
 * ticked once in the window is still in the batch even if others ticked a
 * thousand times after it.
 *
 * Batches hold only the symbols that changed. An empty window emits nothing.
 * When upstream completes, what is pending is flushed before completing;
 * when it fails, the failure propagates.
 */
fun Flow<ServerMessage.Tick>.coalesceLatest(periodMs: Long): Flow<Map<String, ServerMessage.Tick>> =
    channelFlow {
        val lock = Any()
        var pending = HashMap<String, ServerMessage.Tick>()

        // Swaps the map rather than copying it, so the hot path never waits on a copy.
        fun drain(): Map<String, ServerMessage.Tick>? = synchronized(lock) {
            if (pending.isEmpty()) null else pending.also { pending = HashMap(it.size) }
        }

        val upstream = launch {
            collect { tick -> synchronized(lock) { pending[tick.symbol] = tick } }
        }

        while (upstream.isActive) {
            delay(periodMs)
            drain()?.let { send(it) }
        }
        drain()?.let { send(it) }
    }

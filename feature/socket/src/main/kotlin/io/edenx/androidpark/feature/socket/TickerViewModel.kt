package io.edenx.androidpark.feature.socket

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.edenx.androidpark.feature.socket.data.FakeTickerServer
import io.edenx.androidpark.feature.socket.data.TickerClient
import io.edenx.androidpark.feature.socket.data.coalesceLatest
import io.edenx.androidpark.feature.socket.model.ConnectionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

enum class Direction { UP, DOWN, FLAT }

/** One row. Only primitives, so Compose skips a row whose quote did not change. */
@Immutable
data class QuoteUi(val symbol: String, val price: Double, val direction: Direction)

/** Per-second rates, plus running totals for the error counters. */
@Immutable
data class TickerStats(
    val ticksPerSecond: Long = 0,
    val uiUpdatesPerSecond: Long = 0,
    val lost: Long = 0,
    val malformed: Long = 0,
    val reconnects: Long = 0,
)

@Immutable
data class TickerUiState(
    val connection: ConnectionState = ConnectionState.Idle,
    val quotes: List<QuoteUi> = emptyList(),
    val stats: TickerStats = TickerStats(),
    val serverRate: Int = 0,
)

@HiltViewModel
class TickerViewModel @Inject constructor(
    private val client: TickerClient,
    private val server: FakeTickerServer,
) : ViewModel() {

    private val serverRate = MutableStateFlow(server.ticksPerSecond)
    private val uiUpdates = AtomicLong()

    /**
     * Everything up to the final list runs on Default; main only receives a
     * new list UI_PERIOD_MS apart at most, however fast the socket is.
     */
    private val quotes: Flow<List<QuoteUi>> = client.ticks(SYMBOLS)
        .coalesceLatest(UI_PERIOD_MS)
        .scan(emptyMap<String, QuoteUi>()) { rows, batch ->
            rows + batch.mapValues { (symbol, tick) ->
                QuoteUi(symbol, tick.price, direction(rows[symbol]?.price, tick.price))
            }
        }
        // A fixed order, so rows never jump around as prices change.
        .map { rows -> SYMBOLS.mapNotNull(rows::get) }
        .onEach { uiUpdates.incrementAndGet() }
        // Giving up is already visible as ConnectionState.Failed.
        .catch { }
        .flowOn(Dispatchers.Default)

    private val stats: Flow<TickerStats> = flow {
        var lastTicks = client.stats.ticks.get()
        var lastUi = uiUpdates.get()
        while (true) {
            delay(1_000)
            val ticks = client.stats.ticks.get()
            val ui = uiUpdates.get()
            emit(
                TickerStats(
                    ticksPerSecond = ticks - lastTicks,
                    uiUpdatesPerSecond = ui - lastUi,
                    lost = client.stats.lost.get(),
                    malformed = client.stats.malformed.get(),
                    reconnects = client.stats.reconnects.get(),
                ),
            )
            lastTicks = ticks
            lastUi = ui
        }
    }

    /**
     * WhileSubscribed(5s): the socket closes shortly after the screen goes to
     * the background, and survives a rotation without reconnecting.
     */
    val uiState: StateFlow<TickerUiState> = combine(
        quotes.onStart { emit(emptyList()) },
        client.state,
        stats.onStart { emit(TickerStats()) },
        serverRate,
    ) { quotes, connection, stats, rate ->
        TickerUiState(connection, quotes, stats, rate)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TickerUiState())

    fun setServerRate(ticksPerSecond: Int) {
        server.ticksPerSecond = ticksPerSecond
        serverRate.value = ticksPerSecond
    }

    fun dropConnection() = server.dropConnections()

    private fun direction(old: Double?, new: Double) = when {
        old == null || old == new -> Direction.FLAT
        new > old -> Direction.UP
        else -> Direction.DOWN
    }

    companion object {
        const val UI_PERIOD_MS = 100L
        val RATES = listOf(100, 1_000, 5_000, 20_000)
        val SYMBOLS = listOf(
            "AAPL", "MSFT", "GOOG", "AMZN", "META", "NVDA", "TSLA", "NFLX", "AMD", "INTC",
            "ORCL", "CRM", "ADBE", "PYPL", "UBER", "SHOP", "SQ", "COIN", "PLTR", "SNOW",
            "BABA", "JD", "NIO", "SONY", "TM", "SAP", "ASML", "TSM", "V", "MA",
        )
    }
}

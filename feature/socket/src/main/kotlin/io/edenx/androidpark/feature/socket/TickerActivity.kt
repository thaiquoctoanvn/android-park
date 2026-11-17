package io.edenx.androidpark.feature.socket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.edenx.androidpark.core.designsystem.AndroidParkTheme
import io.edenx.androidpark.feature.socket.model.ConnectionState
import java.util.Locale

/**
 * A stream of prices pushed over a WebSocket by a fake server running in this
 * process. Raise the server rate and compare "ticks/s" with "UI updates/s":
 * the first follows the server, the second stays at ~10.
 */
@AndroidEntryPoint
class TickerActivity : ComponentActivity() {

    private val viewModel: TickerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidParkTheme {
                // Lifecycle-aware: stops collecting in the background, which lets
                // WhileSubscribed close the socket.
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                Surface(Modifier.fillMaxSize()) {
                    TickerScreen(
                        state = state,
                        onRateSelected = viewModel::setServerRate,
                        onDropConnection = viewModel::dropConnection,
                    )
                }
            }
        }
    }
}

@Composable
private fun TickerScreen(
    state: TickerUiState,
    onRateSelected: (Int) -> Unit,
    onDropConnection: () -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        ConnectionHeader(state.connection)
        StatsPanel(state.stats)
        Row(
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TickerViewModel.RATES.forEach { rate ->
                FilterChip(
                    selected = rate == state.serverRate,
                    onClick = { onRateSelected(rate) },
                    label = { Text(if (rate >= 1_000) "${rate / 1_000}k/s" else "$rate/s") },
                )
            }
        }
        OutlinedButton(onClick = onDropConnection) { Text("Drop connection") }
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        LazyColumn(Modifier.fillMaxSize()) {
            // A stable key lets Compose reuse each row and recompose only the
            // ones whose QuoteUi actually changed.
            items(state.quotes, key = { it.symbol }) { quote -> QuoteRow(quote) }
        }
    }
}

@Composable
private fun ConnectionHeader(connection: ConnectionState) {
    val (label, color) = when (connection) {
        ConnectionState.Idle -> "Idle" to Color.Gray
        ConnectionState.Connecting -> "Connecting…" to Color(0xFFF9A825)
        ConnectionState.Connected -> "Connected" to Color(0xFF2E7D32)
        is ConnectionState.Retrying -> "Retry #${connection.attempt} in ${connection.inMs} ms" to Color(0xFFEF6C00)
        is ConnectionState.Failed -> "Failed: ${connection.cause.message}" to Color(0xFFC62828)
    }
    Text(label, color = color, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun StatsPanel(stats: TickerStats) {
    Column(Modifier.padding(vertical = 8.dp)) {
        StatLine("ticks/s received", stats.ticksPerSecond)
        StatLine("UI updates/s", stats.uiUpdatesPerSecond)
        StatLine("ticks lost (seq gaps)", stats.lost)
        StatLine("malformed frames", stats.malformed)
        StatLine("reconnects", stats.reconnects)
    }
}

@Composable
private fun StatLine(label: String, value: Long) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text("$value", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun QuoteRow(quote: QuoteUi) {
    val color = when (quote.direction) {
        Direction.UP -> Color(0xFF2E7D32)
        Direction.DOWN -> Color(0xFFC62828)
        Direction.FLAT -> MaterialTheme.colorScheme.onSurface
    }
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(quote.symbol, fontFamily = FontFamily.Monospace)
        Text(
            String.format(Locale.US, "%,.2f %s", quote.price, arrowOf(quote.direction)),
            color = color,
            fontFamily = FontFamily.Monospace,
        )
    }
}

private fun arrowOf(direction: Direction) = when (direction) {
    Direction.UP -> "▲"
    Direction.DOWN -> "▼"
    Direction.FLAT -> " "
}

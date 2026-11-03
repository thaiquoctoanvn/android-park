package io.edenx.androidpark.feature.socket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import io.edenx.androidpark.core.designsystem.AndroidParkTheme

/**
 * A stream of prices pushed over a WebSocket by a fake server running in this
 * process. Placeholder screen until the ViewModel lands.
 */
@AndroidEntryPoint
class TickerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidParkTheme {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Ticker")
                }
            }
        }
    }
}

package io.edenx.androidpark.feature.gesture

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.edenx.androidpark.core.designsystem.AndroidParkTheme

/**
 * The first Compose sample. No BaseActivity, no ViewBinding, no layout XML -
 * a ComponentActivity is all a Compose screen needs.
 */
class MultiTouchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidParkTheme {
                MultiTouchScreen()
            }
        }
    }
}

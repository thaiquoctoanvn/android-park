package io.edenx.androidpark.feature.gesture

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.edenx.androidpark.core.designsystem.AndroidParkTheme
import io.edenx.androidpark.core.model.TouchPointItem
import kotlin.math.roundToInt

/**
 * The View version of this sample logged raw MotionEvent coordinates and drew
 * nothing. Compose makes the interesting part - tracking several pointers at
 * once - small enough that the sample can actually show what it is doing.
 */
@Composable
fun MultiTouchScreen(modifier: Modifier = Modifier) {
    // Keyed by pointer id, so a finger lifting removes only its own circle.
    val pointers = remember { mutableStateMapOf<Long, TouchPointItem>() }
    var scale by remember { mutableStateOf(1f) }
    var rotation by remember { mutableStateOf(0f) }

    Surface(modifier = modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            pointers.clear()
                            event.changes
                                .filter { it.pressed }
                                .forEach {
                                    pointers[it.id.value] =
                                        TouchPointItem(it.position.x, it.position.y)
                                }
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, rotate ->
                        scale = (scale * zoom).coerceIn(0.5f, 5f)
                        rotation += rotate
                    }
                }
        ) {
            PointerCanvas(pointers.values.toList())

            Column(
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(24.dp)
                    .fillMaxWidth(),
            ) {
                Text(
                    text = "Fingers down: ${pointers.size}",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "pinch %.2fx   rotate %d°".format(scale, rotation.roundToInt()),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun PointerCanvas(points: List<TouchPointItem>) {
    val ring = MaterialTheme.colorScheme.primary
    val fill = MaterialTheme.colorScheme.tertiary

    Canvas(
        Modifier
            .fillMaxSize()
            .background(Color.Transparent),
    ) {
        points.forEach { p ->
            val centre = Offset(p.xPoint, p.yPoint)
            drawCircle(color = fill.copy(alpha = 0.25f), radius = 96f, center = centre)
            drawCircle(color = ring, radius = 96f, center = centre, style = Stroke(width = 6f))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MultiTouchScreenPreview() {
    AndroidParkTheme {
        MultiTouchScreen()
    }
}

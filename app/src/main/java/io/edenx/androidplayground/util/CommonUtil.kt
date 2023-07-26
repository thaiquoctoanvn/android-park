package io.edenx.androidplayground.util

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.random.Random

fun openBrowser(url: String, activity: Activity) {
    if (url.startsWith("http://") || url.startsWith("https://")) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
        activity.startActivity(Intent.createChooser(intent, null))
    }
}

fun generateRandomColor(): Int {
    // This is the base color which will be mixed with the generated one
    val baseColor: Int = Color.WHITE
    val baseRed: Int = Color.red(baseColor)
    val baseGreen: Int = Color.green(baseColor)
    val baseBlue: Int = Color.blue(baseColor)
    val red: Int = (baseRed + Random.nextInt(256)) / 2
    val green: Int = (baseGreen + Random.nextInt(256)) / 2
    val blue: Int = (baseBlue + Random.nextInt(256)) / 2
    return Color.argb(255, red, green, blue)
}
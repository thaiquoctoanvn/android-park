package io.edenx.androidpark.util

import android.app.Activity
import android.content.Intent
import android.net.Uri

fun openBrowser(url: String, activity: Activity) {
    if (url.startsWith("http://") || url.startsWith("https://")) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
        activity.startActivity(Intent.createChooser(intent, null))
    }
}
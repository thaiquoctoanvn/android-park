package io.edenx.androidpark.feature.camera

import android.content.Context
import android.content.Intent

/** Which of the two camera samples CameraActivity should host. */
enum class CameraScreenType {
    IMG_LABELING,
    QR_DETECTING,
}

/**
 * The only supported way in. The caller used to build the Intent itself and
 * put a raw "type" string extra, which meant the key was part of the contract
 * and nothing checked the value.
 */
object CameraNavigation {
    private const val EXTRA_TYPE = "type"

    fun intent(context: Context, type: CameraScreenType): Intent =
        Intent(context, CameraActivity::class.java)
            .putExtra(EXTRA_TYPE, type.name)

    internal fun typeOf(intent: Intent): CameraScreenType? =
        intent.getStringExtra(EXTRA_TYPE)?.let { runCatching { CameraScreenType.valueOf(it) }.getOrNull() }
}

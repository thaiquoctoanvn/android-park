package io.edenx.androidpark.core.common

/**
 * Build information that only the application module can know.
 *
 * A library module cannot read `BuildConfig.DEBUG` meaningfully: AGP 8
 * disables BuildConfig for libraries by default, and even when enabled the
 * flag reflects the *library* variant rather than the build type of the app
 * consuming it. Anything that branches on debug-vs-release asks for this
 * instead, and :app supplies the real values.
 */
interface AppConfig {
    val isDebug: Boolean
    val applicationId: String
    val versionName: String
}

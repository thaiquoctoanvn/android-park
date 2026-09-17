package io.edenx.androidpark.core.analytics

/**
 * Resolved once at injection time instead of branching on a debug flag at
 * every call site. That branch used to live inside AdUtil and silently
 * overrode the ad unit the caller passed in.
 */
data class AdIds(
    val banner: String,
    val interstitial: String,
    val rewarded: String,
    val appOpen: String,
)

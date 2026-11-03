package io.edenx.androidpark.feature.socket.model

sealed interface ConnectionState {
    data object Idle : ConnectionState
    data object Connecting : ConnectionState
    data object Connected : ConnectionState

    /** Waiting [inMs] before reconnect [attempt], counted from 1. */
    data class Retrying(val attempt: Int, val inMs: Long) : ConnectionState

    data class Failed(val cause: Throwable) : ConnectionState
}

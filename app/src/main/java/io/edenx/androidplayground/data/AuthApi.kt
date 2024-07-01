package io.edenx.androidplayground.data

import io.edenx.androidplayground.data.model.*
import retrofit2.http.*

interface AuthApi {
    @POST("chat/completions")
    suspend fun chatWithGpt(@Body request: ChatRequest): Result<ChatResponse>

    @POST("completions")
    suspend fun askCompletion(@Body request: CompletionRequest): Result<CompletionResponse>
}
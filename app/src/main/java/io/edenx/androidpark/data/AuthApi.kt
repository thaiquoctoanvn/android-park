package io.edenx.androidpark.data

import io.edenx.androidpark.data.model.*
import retrofit2.http.*

interface AuthApi {
    @POST("chat/completions")
    suspend fun chatWithGpt(@Body request: ChatRequest): Result<ChatResponse>

    @POST("completions")
    suspend fun askCompletion(@Body request: CompletionRequest): Result<CompletionResponse>
}
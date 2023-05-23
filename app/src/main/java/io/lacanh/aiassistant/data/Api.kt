package io.lacanh.aiassistant.data

import io.lacanh.aiassistant.data.model.*
import retrofit2.http.*

interface Api {
    @POST("chat/completions")
    suspend fun chatWithGpt(@Body request: ChatRequest): Result<ChatResponse>

    @POST("completions")
    suspend fun askCompletion(@Body request: CompletionRequest): Result<CompletionResponse>

    @GET("v1/images/search")
    suspend fun getDoggoImages(
        @Query("page") page: Int,
        @Query("limit") size: Int
    ): Result<List<DoggoImageModel>>
}
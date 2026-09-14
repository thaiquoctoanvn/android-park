package io.edenx.androidpark.core.network

import io.edenx.androidpark.core.model.DoggoImageModel
import retrofit2.http.GET
import retrofit2.http.Query

interface DogApi {
    @GET("images/search")
    suspend fun getDoggoImages(
        @Query("page") page: Int,
        @Query("limit") size: Int
    ): Result<List<DoggoImageModel>>
}

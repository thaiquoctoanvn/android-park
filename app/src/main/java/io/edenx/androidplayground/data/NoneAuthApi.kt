package io.edenx.androidplayground.data

import io.edenx.androidplayground.data.model.DoggoImageModel
import retrofit2.http.GET
import retrofit2.http.Query

interface NoneAuthApi {
    @GET("images/search")
    suspend fun getDoggoImages(
        @Query("page") page: Int,
        @Query("limit") size: Int
    ): Result<List<DoggoImageModel>>
}

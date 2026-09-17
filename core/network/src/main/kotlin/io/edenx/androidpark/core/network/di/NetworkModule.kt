package io.edenx.androidpark.core.network.di

import android.util.Log
import com.moczul.ok2curl.CurlInterceptor
import com.moczul.ok2curl.logger.Logger
import com.skydoves.retrofit.adapters.result.ResultCallAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.edenx.androidpark.core.network.DogApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Only one API is left after the dead OpenAI client was removed, so the
 * @AuthRetrofit / @NoneAuthRetrofit qualifier pairs are gone with it.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            CurlInterceptor(object : Logger {
                override fun log(message: String) {
                    Log.v("curl", message)
                }
            })
        )
        .addInterceptor(
            HttpLoggingInterceptor { Log.v("xxxx", it) }
                .apply { setLevel(HttpLoggingInterceptor.Level.BASIC) }
        )
        .connectTimeout(60, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(ResultCallAdapterFactory.create())
        .baseUrl("https://api.thedogapi.com/v1/")
        .client(okHttpClient)
        .build()

    @Provides
    @Singleton
    fun provideDogApi(retrofit: Retrofit): DogApi = retrofit.create(DogApi::class.java)
}

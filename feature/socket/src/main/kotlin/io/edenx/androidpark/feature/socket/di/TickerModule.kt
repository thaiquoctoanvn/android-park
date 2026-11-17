package io.edenx.androidpark.feature.socket.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.edenx.androidpark.feature.socket.data.FakeTickerServer
import io.edenx.androidpark.feature.socket.data.TickerClient
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TickerModule {

    @Provides
    @Singleton
    fun provideFakeTickerServer(): FakeTickerServer = FakeTickerServer()

    /**
     * Its own OkHttpClient rather than the one :core:network provides: that one
     * carries curl/logging interceptors meant for REST calls, and the ping
     * interval below only makes sense for a long-lived socket.
     */
    @Provides
    @Singleton
    fun provideTickerClient(server: FakeTickerServer): TickerClient = TickerClient(
        okHttp = OkHttpClient.Builder()
            // A socket that stops answering pings is dead even if TCP hasn't noticed.
            .pingInterval(10, TimeUnit.SECONDS)
            .build(),
        endpoint = server::url,
    )
}

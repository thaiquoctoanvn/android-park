package io.edenx.androidplayground.di

import android.content.Context
import android.util.Log
import com.moczul.ok2curl.CurlInterceptor
import com.moczul.ok2curl.logger.Logger
import com.skydoves.retrofit.adapters.result.ResultCallAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.edenx.androidplayground.component.paging.PagingRepo
import io.edenx.androidplayground.data.NoneAuthApi
import io.edenx.androidplayground.util.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    @NoneAuthInterceptorOkHttpClient
    @Provides
    fun provideNoneAuthOkHttpClient() = OkHttpClient.Builder()
        .addInterceptor(
            CurlInterceptor(object : Logger {
                override fun log(message: String) {
                    Log.v("curl", message)
                }
            })
        )
        .addInterceptor(HttpLoggingInterceptor {
            Log.v("xxxx", it)
        }.apply { setLevel(HttpLoggingInterceptor.Level.BASIC) })
        .connectTimeout(60, TimeUnit.SECONDS)
        .build()

    @NoneAuthRetrofit
    @Provides
    fun provideNoneAuthRetrofit(@NoneAuthInterceptorOkHttpClient okHttpClient: OkHttpClient) = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(ResultCallAdapterFactory.create())
        .baseUrl("https://api.thedogapi.com/v1/")
        .client(okHttpClient)
        .build()

    @Singleton
    @Provides
    fun provideNoneAuthApi(@NoneAuthRetrofit retrofit: Retrofit) = retrofit.create(NoneAuthApi::class.java)

    /////////////////////////

    @Singleton
    @Provides
    fun providePagingRepo(api: NoneAuthApi) = PagingRepo(api)

    @Singleton
    @Provides
    fun provideAdeUtil(@ApplicationContext applicationContext: Context) = AdUtil(applicationContext)

    @Singleton
    @Provides
    fun providePurchasesUpdatedListenerImpl() = PurchasesUpdatedListenerImpl()

    @Singleton
    @Provides
    fun provideBillingUtil(@ApplicationContext applicationContext: Context, listenerImpl: PurchasesUpdatedListenerImpl) = BillingUtil(applicationContext, listenerImpl)

    @Singleton
    @Provides
    fun provideGooglePayUtil(@ApplicationContext applicationContext: Context) = GooglePayUtil(applicationContext)
}
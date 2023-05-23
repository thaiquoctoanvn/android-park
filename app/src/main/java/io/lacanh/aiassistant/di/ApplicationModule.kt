package io.lacanh.aiassistant.di

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.moczul.ok2curl.CurlInterceptor
import com.moczul.ok2curl.logger.Logger
import com.skydoves.retrofit.adapters.result.ResultCallAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.lacanh.aiassistant.component.paging.PagingRepo
import io.lacanh.aiassistant.data.Api
import io.lacanh.aiassistant.data.repo.NetworkRepo
import io.lacanh.aiassistant.util.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    @Singleton
    @Provides
    fun provideOkHttpClient() = OkHttpClient.Builder()
//        .addInterceptor(Interceptor {
//            it.proceed(it.request().newBuilder()
//                .addHeader("Content-Type", "application/json")
//                .addHeader("Authorization" , "Bearer ${Firebase.remoteConfig.getString(OPEN_AI_API_KEY)}")
//                .build()
//            )
//        })
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

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient) = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(ResultCallAdapterFactory.create())
        .baseUrl("https://api.thedogapi.com/")
        .client(okHttpClient)
        .build()

    @Singleton
    @Provides
    fun provideApi(retrofit: Retrofit) = retrofit.create(Api::class.java)

    @Singleton
    @Provides
    fun provideNetworkRepo(api: Api) = NetworkRepo(api)

    @Singleton
    @Provides
    fun providePagingRepo(api: Api) = PagingRepo(api)

    @Provides
    @Singleton
    fun provideSharedPreferenceInstance(@ApplicationContext applicationContext: Context): SharedPreferences =
        applicationContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideSharedPreferenceUtil(sharedPreferences: SharedPreferences) = SharedPrefUtil(sharedPreferences)

    @Singleton
    @Provides
    fun provideAdeUtil(@ApplicationContext applicationContext: Context) = AdUtil(applicationContext)

    @Singleton
    @Provides
    fun providePurchasesUpdatedListenerImpl() = PurchasesUpdatedListenerImpl()

    @Singleton
    @Provides
    fun provideBillingUtil(@ApplicationContext applicationContext: Context, listenerImpl: PurchasesUpdatedListenerImpl) = BillingUtil(applicationContext, listenerImpl)
}
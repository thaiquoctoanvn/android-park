package io.edenx.androidplayground.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.edenx.androidpark.core.network.DogApi
import io.edenx.androidplayground.component.paging.PagingRepo
import io.edenx.androidplayground.util.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    @Singleton
    @Provides
    fun providePagingRepo(api: DogApi) = PagingRepo(api)

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
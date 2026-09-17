package io.edenx.androidpark.core.common.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.edenx.androidpark.core.common.SHARED_PREF_NAME
import io.edenx.androidpark.core.common.SharedPrefUtil
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideSharedPrefUtil(sharedPreferences: SharedPreferences) =
        SharedPrefUtil(sharedPreferences)
}

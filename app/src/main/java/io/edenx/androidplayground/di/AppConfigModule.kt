package io.edenx.androidplayground.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.edenx.androidpark.core.common.AppConfig
import io.edenx.androidplayground.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The one place BuildConfig is read. Library modules ask for AppConfig.
 */
class DefaultAppConfig @Inject constructor() : AppConfig {
    override val isDebug = BuildConfig.DEBUG
    override val applicationId = BuildConfig.APPLICATION_ID
    override val versionName = BuildConfig.VERSION_NAME
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AppConfigModule {
    @Binds
    @Singleton
    abstract fun bindAppConfig(impl: DefaultAppConfig): AppConfig
}

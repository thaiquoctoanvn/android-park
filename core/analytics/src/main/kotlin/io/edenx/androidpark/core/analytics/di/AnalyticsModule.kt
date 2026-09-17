package io.edenx.androidpark.core.analytics.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.edenx.androidpark.core.analytics.APP_OPEN_AD_KEY
import io.edenx.androidpark.core.analytics.AdIds
import io.edenx.androidpark.core.analytics.BANNER_AD_KEY
import io.edenx.androidpark.core.analytics.INTERSTITIAL_AD_KEY
import io.edenx.androidpark.core.analytics.PUBLISHER_ID_KEY
import io.edenx.androidpark.core.analytics.REWARDED_AD_KEY
import io.edenx.androidpark.core.analytics.RemoteConfigProvider
import io.edenx.androidpark.core.common.AppConfig
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {

    /** Google's public test publisher id. */
    private const val TEST_PUBLISHER = "ca-app-pub-3940256099942544"

    @Provides
    @Singleton
    fun provideAdIds(
        appConfig: AppConfig,
        remoteConfig: RemoteConfigProvider,
    ): AdIds = if (appConfig.isDebug) {
        AdIds(
            banner = "$TEST_PUBLISHER/6300978111",
            interstitial = "$TEST_PUBLISHER/1033173712",
            rewarded = "$TEST_PUBLISHER/5224354917",
            appOpen = "$TEST_PUBLISHER/3419835294",
        )
    } else {
        val publisher = remoteConfig.getString(PUBLISHER_ID_KEY)
        AdIds(
            banner = "$publisher/${remoteConfig.getString(BANNER_AD_KEY)}",
            interstitial = "$publisher/${remoteConfig.getString(INTERSTITIAL_AD_KEY)}",
            rewarded = "$publisher/${remoteConfig.getString(REWARDED_AD_KEY)}",
            appOpen = "$publisher/${remoteConfig.getString(APP_OPEN_AD_KEY)}",
        )
    }
}

package io.edenx.androidplayground

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.appopen.AppOpenAd
import dagger.hilt.android.HiltAndroidApp
import io.edenx.androidpark.core.analytics.AdUtil
import io.edenx.androidpark.core.analytics.APP_OPEN_AD_KEY
import io.edenx.androidpark.core.analytics.RemoteConfigProvider
import io.edenx.androidpark.core.common.SharedPrefUtil
import java.util.*
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    @Inject lateinit var sharedPrefUtil: SharedPrefUtil
    @Inject lateinit var adUtil: AdUtil
    @Inject lateinit var remoteConfigProvider: RemoteConfigProvider
    private lateinit var appOpenAdManager: AppOpenAdManager

    private var currentActivity: Activity? = null
    private var loadTime: Long = 0
    override fun onCreate() {
        super<Application>.onCreate()
        registerActivityLifecycleCallbacks(this)
        MobileAds.initialize(this) {}
        if (BuildConfig.DEBUG) {
            val configuration = RequestConfiguration.Builder().setTestDeviceIds(
                listOf(
                    "F563823037B8A7AA95B8A32999972C17",
                    "7F9A797ED00D926234EEC1F7288CFD5E"
                )
            ).build()
            MobileAds.setRequestConfiguration(configuration)
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        remoteConfigProvider.fetch()
        appOpenAdManager = AppOpenAdManager()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

    }

    override fun onActivityStarted(activity: Activity) {
        if (this::appOpenAdManager.isInitialized && !appOpenAdManager.isShowingAd) {
            currentActivity = activity
        }
    }

    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {

    }

    override fun onStart(owner: LifecycleOwner) {
        currentActivity?.let {
            //sharedPrefUtil.setAppOpenTime(sharedPrefUtil.getAppOpenTime() + 1)
//            appOpenAdManager.showAdIfAvailable(it, object : OnShowAdCompleteListener {
//                override fun onShowAdComplete() {
//
//                }
//            })
        }
    }

    private inner class AppOpenAdManager {
        private var appOpenAd: AppOpenAd? = null
        private var isLoadingAd = false
        var isShowingAd = false

        fun loadAd(context: Context) {
            if (isLoadingAd || isAdAvailable()) {
                return
            }
            isLoadingAd = true
            adUtil.loadAppOpenAd(
                context = context,
                fullAdId = remoteConfigProvider.getString(APP_OPEN_AD_KEY),
                mOnAdLoaded = {
                    appOpenAd = it
                    isLoadingAd = false
                    loadTime = Date().time
                    currentActivity?.let { it1 -> appOpenAd?.show(it1) }
                },
                mOnAdFailedToLoad = {
                    isLoadingAd = false
                }
            )
        }

        private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
            val dateDifference: Long = Date().time - loadTime
            val numMilliSecondsPerHour: Long = 3600000
            return dateDifference < numMilliSecondsPerHour * numHours
        }
        private fun isAdAvailable(): Boolean {
            return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4) && sharedPrefUtil.checkIfShowAppOpenAd()
        }

        fun showAdIfAvailable(
            activity: Activity,
            onShowAdCompleteListener: OnShowAdCompleteListener
        ) {
            if (sharedPrefUtil.getIsBillingPurchased()) {
                Log.d("xxxx", "User has paid for subscription, ignore ads")
                return
            }
            if (isShowingAd) {
                Log.d("xxxx", "The app open ad is already showing.")
                return
            }
            if (!isAdAvailable()) {
                Log.d("xxxx", "The app open ad is not ready yet.")
                onShowAdCompleteListener.onShowAdComplete()
                loadAd(activity)
                return
            }
            appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    isShowingAd = false
                    sharedPrefUtil.setAppOpenTime(1)
                    onShowAdCompleteListener.onShowAdComplete()
                    loadAd(activity)
                }
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.d("xxxx", adError.message)
                    appOpenAd = null
                    isShowingAd = false

                    onShowAdCompleteListener.onShowAdComplete()
                    loadAd(activity)
                }

                override fun onAdShowedFullScreenContent() {

                }
            }
            isShowingAd = true
            appOpenAd?.show(activity)
        }
    }
}

interface OnShowAdCompleteListener {
    fun onShowAdComplete()
}
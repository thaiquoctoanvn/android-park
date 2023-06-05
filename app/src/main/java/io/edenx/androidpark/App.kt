package io.edenx.androidpark

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.android.billingclient.api.BillingClient
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import dagger.hilt.android.HiltAndroidApp
import io.edenx.androidpark.util.*
import java.util.*
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Application.ActivityLifecycleCallbacks, LifecycleObserver {

    @Inject lateinit var sharedPrefUtil: SharedPrefUtil
    @Inject lateinit var adUtil: AdUtil
    @Inject lateinit var billingUtil: BillingUtil
    private lateinit var appOpenAdManager: AppOpenAdManager

    private var currentActivity: Activity? = null
    private var loadTime: Long = 0
    override fun onCreate() {
        super.onCreate()
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
        fetchFRC()
        appOpenAdManager = AppOpenAdManager()
    }

    private fun fetchFRC() {
        Firebase.remoteConfig.apply {
            setConfigSettingsAsync(remoteConfigSettings {
                minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
            })
            setDefaultsAsync(R.xml.remote_config_defaults)
            fetchAndActivate()
                .addOnCompleteListener {
                    if (it.isSuccessful)
                        Log.d(
                            "xxxx",
                            "Remote config fetched: ${
                                Firebase.remoteConfig.getString(
                                    OPEN_AI_API_KEY
                                )}"
                        )
                }
                .addOnFailureListener {
                    it.printStackTrace()
                    Log.d("xxxx", "Remote config fetching error: ${it.message}")
                }
        }
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

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
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
                fullAdId = "${Firebase.remoteConfig.getString(PUBLISHER_ID_KEY)}/${
                    Firebase.remoteConfig.getString(
                        APP_OPEN_AD_KEY
                    )
                }",
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
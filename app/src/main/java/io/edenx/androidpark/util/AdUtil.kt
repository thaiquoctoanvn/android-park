package io.edenx.androidpark.util

import android.content.Context
import android.util.Log
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import io.edenx.androidpark.BuildConfig

class AdUtil(context: Context) {

    companion object {
        const val interstitialAdTest = "1033173712"
        const val bannerAdTest = "6300978111"
        const val rewardedAdTest = "5224354917"
        const val appOpenAdTest = "3419835294"
        const val publisherIdTest = "ca-app-pub-3940256099942544"
    }

    var rewardedAd: RewardedAd? = null

    init {

        loadRewardedAd(
            context = context,
            fullAdId = if (BuildConfig.DEBUG) "$publisherIdTest/$rewardedAdTest" else "${Firebase.remoteConfig.getString(PUBLISHER_ID_KEY)}/${
                Firebase.remoteConfig.getString(
                    REWARDED_AD_KEY
                )
            }",
            mOnAdLoaded = {
                rewardedAd = it
            }
        )
    }

    fun loadInterstitialAd(
        context: Context,
        fullAdId: String,
        mOnAdLoaded: ((InterstitialAd) -> Unit)? = null,
        mOnAdFailedToLoad: ((LoadAdError) -> Unit)? = null
    ) {
        InterstitialAd.load(
            context,
            if (BuildConfig.DEBUG) "$publisherIdTest/$interstitialAdTest" else fullAdId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    mOnAdLoaded?.let { it(ad) }
                    Log.d("xxxx", "InterstitialAd loaded")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mOnAdFailedToLoad?.let { it(adError) }
                    Log.d("xxxx", "InterstitialAd failed to load: ${adError.message}")
                }
            }
        )
    }

    fun loadBannerAd(
        adContainer: FrameLayout,
        fullAdId: String,
        onAdClosed: () -> Unit
    ): AdView {
        val adView = AdView(adContainer.context).apply {
            adUnitId = if (BuildConfig.DEBUG) "$publisherIdTest/$bannerAdTest" else fullAdId
            setAdSize(AdSize.BANNER)
        }
        adContainer.addView(adView)
        adView.post {
            adView.adListener = object : AdListener() {
                override fun onAdClosed() {
                    onAdClosed()
                    adContainer.removeAllViews()
                    adContainer.isVisible = false
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    onAdClosed()
                    Log.e("xxxx", "Banner ad failed to load: ${adError.message}")
                    adContainer.removeAllViews()
                    adContainer.isVisible = false
                }
            }
        }
        adView.loadAd(AdRequest.Builder().build())
        return adView
    }

    fun loadRewardedAd(
        context: Context,
        fullAdId: String,
        mOnAdLoaded: ((RewardedAd) -> Unit)? = null,
        mOnAdFailedToLoad: ((LoadAdError) -> Unit)? = null
    ) {
        RewardedAd.load(
            context,
            if (BuildConfig.DEBUG) "$publisherIdTest/$rewardedAdTest" else fullAdId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    mOnAdLoaded?.let { it(ad) }
                    Log.d("xxxx", "RewardedAd loaded")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mOnAdFailedToLoad?.let { it(adError) }
                    Log.d("xxxx", "RewardedAd failed to load: ${adError.message}")
                }
            }
        )
    }

    fun loadAppOpenAd(
        context: Context,
        fullAdId: String,
        mOnAdLoaded: ((AppOpenAd) -> Unit)? = null,
        mOnAdFailedToLoad: ((LoadAdError) -> Unit)? = null
    ) {
        AppOpenAd.load(
            context, if (BuildConfig.DEBUG) "$publisherIdTest/$appOpenAdTest" else fullAdId, AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.d("xxxx", "AppOpenAd loaded")
                    mOnAdLoaded?.let { it(ad) }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.d("xxxx", "AppOpenAd failed to load: ${loadAdError.message}")
                    mOnAdFailedToLoad?.let { it(loadAdError) }
                }
            }
        )
    }
}
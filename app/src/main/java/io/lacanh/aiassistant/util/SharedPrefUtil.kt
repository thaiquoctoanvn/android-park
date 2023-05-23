package io.lacanh.aiassistant.util

import android.content.SharedPreferences
import androidx.core.content.edit

class SharedPrefUtil(private val sharedPreferences: SharedPreferences) {

    fun checkIfShowInterstitialAd(
        onPreLoadAd: () -> Unit = {}
    ): Boolean {
        val turn = sharedPreferences.getInt(TOPIC_OPEN_COUNT, 1)
        if (turn in 2..2) onPreLoadAd()
        if (turn >= 3) {
            setTopicOpenTurn(1)
            return true
        }
        setTopicOpenTurn(turn + 1)
        return false
    }

    fun setTopicOpenTurn(value: Int) {
        sharedPreferences.edit {
            putInt(TOPIC_OPEN_COUNT, value)
            apply()
        }
    }

    fun checkIfShowRewardedAd(): Boolean {
        val turn = sharedPreferences.getInt(TOPIC_USAGE_TURN, 3)
        if (turn == 0) return true
        setTopicUsageTurn(turn - 1)
        return false
    }

    fun setTopicUsageTurn(value: Int) {
        sharedPreferences.edit {
            putInt(TOPIC_USAGE_TURN, value)
            apply()
        }
    }

    fun getTopicUsageTurn(): Int {
        return sharedPreferences.getInt(TOPIC_USAGE_TURN, 5)
    }

    fun checkIfShowAppOpenAd(): Boolean {
        return true
    }

    fun getAppOpenTime(): Int {
        return sharedPreferences.getInt(APP_OPEN_TIME, 1)
    }

    fun setAppOpenTime(value: Int) {
        sharedPreferences.edit {
            putInt(APP_OPEN_TIME, value)
            apply()
        }
    }

    fun getIsBillingPurchased(): Boolean {
        return sharedPreferences.getBoolean(IS_BILLING_PURCHASED, true)
    }

    fun setIsBillingPurchased(value: Boolean) {
        sharedPreferences.edit {
            putBoolean(IS_BILLING_PURCHASED, value)
            apply()
        }
    }
}
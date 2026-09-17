package io.edenx.androidpark.core.analytics

import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import io.edenx.androidpark.core.common.AppConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the Remote Config defaults and the fetch that App.kt used to run
 * inline, so nothing outside this module has to know Firebase is involved.
 */
@Singleton
class RemoteConfigProvider @Inject constructor(
    private val appConfig: AppConfig,
) {
    private val remoteConfig get() = Firebase.remoteConfig

    fun getString(key: String): String = remoteConfig.getString(key)

    fun fetch() {
        remoteConfig.apply {
            setConfigSettingsAsync(
                remoteConfigSettings {
                    minimumFetchIntervalInSeconds = if (appConfig.isDebug) 0 else 3600
                }
            )
            setDefaultsAsync(R.xml.remote_config_defaults)
            fetchAndActivate()
                .addOnCompleteListener {
                    if (it.isSuccessful) Log.d(TAG, "Remote config fetched")
                }
                .addOnFailureListener {
                    Log.d(TAG, "Remote config fetching error: ${it.message}")
                }
        }
    }

    private companion object {
        const val TAG = "RemoteConfig"
    }
}

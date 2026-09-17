import com.android.build.api.dsl.ApplicationExtension
import io.edenx.androidpark.configureKotlinAndroid
import io.edenx.androidpark.libs
import io.edenx.androidpark.pluginId
import io.edenx.androidpark.versionInt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply(libs.pluginId("android-application"))
            apply(libs.pluginId("kotlin-android"))
        }

        extensions.configure<ApplicationExtension> {
            configureKotlinAndroid(this)
            defaultConfig.targetSdk = libs.versionInt("targetSdk")
            defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

            // App-level only: App.kt reads BuildConfig.DEBUG and APP_NAME.
            buildFeatures.buildConfig = true
        }
    }
}

import com.android.build.gradle.LibraryExtension
import io.edenx.androidpark.configureKotlinAndroid
import io.edenx.androidpark.lib
import io.edenx.androidpark.libs
import io.edenx.androidpark.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply(libs.pluginId("android-library"))
            apply(libs.pluginId("kotlin-android"))
        }

        extensions.configure<LibraryExtension> {
            configureKotlinAndroid(this)
            defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

            // R8 runs whole-program from :app, so per-library minification is
            // wasted work. A module that needs keep rules contributes them
            // through consumerProguardFiles instead.
            buildTypes.getByName("release") { isMinifyEnabled = false }
        }

        dependencies {
            add("implementation", libs.lib("androidx-core-ktx"))
            add("testImplementation", libs.lib("junit4"))
        }
    }
}

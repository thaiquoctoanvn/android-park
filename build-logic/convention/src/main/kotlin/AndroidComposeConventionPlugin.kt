import com.android.build.gradle.LibraryExtension
import io.edenx.androidpark.configureAndroidCompose
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * The Compose half of the UI split. A module applies this OR
 * androidpark.android.view, or both when it needs AndroidView interop.
 */
class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        extensions.configure<LibraryExtension> {
            configureAndroidCompose(this)
        }
    }
}

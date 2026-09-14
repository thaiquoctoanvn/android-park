import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * The View half of the UI split. Applied by feature modules that still use
 * XML layouts + ViewBinding; Compose modules apply androidpark.android.compose
 * instead, so neither pays for the other.
 */
class AndroidViewConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        extensions.configure<LibraryExtension> {
            buildFeatures.viewBinding = true
        }
    }
}

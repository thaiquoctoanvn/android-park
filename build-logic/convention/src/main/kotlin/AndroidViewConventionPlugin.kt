import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

/**
 * The View half of the UI split: ViewBinding plus the base Activity/Fragment
 * classes. A module applies this OR androidpark.android.compose, or both when
 * it needs AndroidView interop.
 */
class AndroidViewConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        extensions.configure<LibraryExtension> {
            buildFeatures.viewBinding = true
        }
        dependencies {
            // `api` so a feature sees BaseActivity and, through :core:ui, the
            // design system.
            add("api", project(":core:ui"))
        }
    }
}

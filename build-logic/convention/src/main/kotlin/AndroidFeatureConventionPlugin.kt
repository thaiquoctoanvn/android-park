import io.edenx.androidpark.bundle
import io.edenx.androidpark.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

/**
 * What every sample module needs regardless of UI toolkit.
 *
 * Deliberately does NOT apply Hilt: only a handful of features use it, and
 * applying KSP to the rest costs build time for nothing. Those add
 * androidpark.android.hilt themselves.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("androidpark.android.library")

        dependencies {
            // `api` so features can reference theme resources without repeating
            // the dependency.
            add("api", project(":core:designsystem"))
            add("implementation", project(":core:common"))
            add("implementation", project(":core:model"))

            add("implementation", libs.bundle("androidx-base"))
            add("implementation", libs.bundle("lifecycle"))
        }
    }
}

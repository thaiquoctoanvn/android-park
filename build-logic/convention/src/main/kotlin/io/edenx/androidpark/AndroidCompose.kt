package io.edenx.androidpark

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Compose support, opt-in per module.
 *
 * From Kotlin 2.0 the Compose compiler ships with the Kotlin release and is
 * applied through org.jetbrains.kotlin.plugin.compose, so there is no
 * `composeOptions.kotlinCompilerExtensionVersion` to keep in sync with the
 * Kotlin version. Raising `kotlin` in the catalog raises the compiler too.
 */
internal fun Project.configureAndroidCompose(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    pluginManager.apply(libs.pluginId("kotlin-compose"))

    commonExtension.apply {
        buildFeatures.compose = true
    }

    dependencies {
        val bom = libs.lib("androidx-compose-bom")
        add("implementation", platform(bom))
        add("androidTestImplementation", platform(bom))

        add("implementation", libs.lib("androidx-compose-ui"))
        add("implementation", libs.lib("androidx-compose-ui-graphics"))
        add("implementation", libs.lib("androidx-compose-ui-tooling-preview"))
        add("implementation", libs.lib("androidx-compose-material3"))
        add("implementation", libs.lib("androidx-activity-compose"))
        add("implementation", libs.lib("androidx-lifecycle-runtime-compose"))

        // Previews and the layout inspector only need to exist in debug builds.
        add("debugImplementation", libs.lib("androidx-compose-ui-tooling"))
        add("debugImplementation", libs.lib("androidx-compose-ui-test-manifest"))
    }

    configureComposeMetrics()
}

/**
 * Opt in with -Pandroidpark.enableComposeMetrics=true when chasing
 * recomposition. Off by default because it slows every compile.
 */
private fun Project.configureComposeMetrics() {
    if (providers.gradleProperty("androidpark.enableComposeMetrics").orNull != "true") return

    tasks.withType(KotlinCompile::class.java).configureEach {
        val dir = layout.buildDirectory.dir("compose-metrics").get().asFile.absolutePath
        compilerOptions.freeCompilerArgs.addAll(
            "-P", "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=$dir",
            "-P", "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=$dir",
        )
    }
}

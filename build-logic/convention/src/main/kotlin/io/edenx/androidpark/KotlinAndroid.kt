package io.edenx.androidpark

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Was VERSION_1_8 in the single-module build. 17 is safe with minSdk 24:
 * bytecode level and minSdk are independent once D8 desugars. If anything
 * breaks, this constant and KOTLIN_JVM_TARGET below are the only revert points.
 */
private val JAVA_VERSION = JavaVersion.VERSION_17
private val KOTLIN_JVM_TARGET = JvmTarget.JVM_17

/**
 * Shared by the application and library convention plugins.
 *
 * Note: CommonExtension takes 6 type parameters as of AGP 8.1+ (5 in AGP 7.x).
 *
 * Deliberately does NOT enable viewBinding or compose: a module opts into one
 * (or both) through androidpark.android.view / androidpark.android.compose, so
 * no module pays for a UI toolkit it does not use.
 */
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    commonExtension.apply {
        compileSdk = libs.versionInt("compileSdk")

        defaultConfig {
            minSdk = libs.versionInt("minSdk")
        }

        compileOptions {
            sourceCompatibility = JAVA_VERSION
            targetCompatibility = JAVA_VERSION
        }

        buildFeatures {
            // Only :app overrides this; a library BuildConfig.DEBUG reflects the
            // library variant, not the consuming app, so it is a trap.
            buildConfig = false
        }

        lint {
            // The module split will surface a wave of pre-existing warnings.
            // Keep the build usable now; tighten once the split has landed.
            abortOnError = false
            checkDependencies = true
        }
    }

    configureKotlin()

    dependencies {
        add("implementation", libs.lib("kotlinx-coroutines-android"))
    }
}

/**
 * Configured through KotlinCompile tasks rather than the `kotlin { }` extension
 * so it also covers KSP stub-generation tasks, which otherwise keep the default
 * target and produce "Inconsistent JVM-target compatibility" failures.
 */
private fun Project.configureKotlin() {
    tasks.withType(KotlinCompile::class.java).configureEach {
        compilerOptions {
            jvmTarget.set(KOTLIN_JVM_TARGET)
            freeCompilerArgs.addAll(
                "-Xjvm-default=all",
                // :feature:media uses media3 APIs marked unstable
                "-opt-in=androidx.media3.common.util.UnstableApi",
            )
        }
    }
}

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "io.edenx.androidpark.buildlogic"

// AGP 8.7.3 needs JDK 17+ to run Gradle, so build-logic must emit bytecode
// that same daemon can load.
java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(17)) }
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}

dependencies {
    // compileOnly, NOT implementation: these plugins are supplied at runtime by
    // the consuming build. `implementation` puts a second copy of AGP on the
    // classpath and yields "class AppPlugin loaded by two loaders".
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.hilt.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "androidpark.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "androidpark.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidFeature") {
            id = "androidpark.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("androidView") {
            id = "androidpark.android.view"
            implementationClass = "AndroidViewConventionPlugin"
        }
        register("androidCompose") {
            id = "androidpark.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("androidHilt") {
            id = "androidpark.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }
        register("jvmLibrary") {
            id = "androidpark.jvm.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
    }
}

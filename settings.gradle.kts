pluginManagement {
    // Must sit inside pluginManagement so `id("androidpark.android.library")`
    // resolves against the included build.
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // com.github.mrmike:ok2curl and com.github.skydoves:retrofit-adapters-result
        // are published only here. Listing the exact groups keeps jitpack out of
        // the resolution path for the other ~40 dependencies.
        maven("https://jitpack.io") {
            content {
                includeGroup("com.github.mrmike")
                includeGroup("com.github.skydoves")
            }
        }
    }
}

rootProject.name = "Android Playground"

include(":app")

include(":core:analytics")
include(":core:common")
include(":core:designsystem")
include(":core:model")
include(":core:network")
include(":core:ui")

include(":feature:webview")
include(":feature:backstack")
include(":feature:gesture")

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    // Re-imports the SAME toml the main build uses, so there is exactly one
    // source of truth. Without this, `named("libs")` inside a convention
    // plugin throws "Catalog named libs doesn't exist".
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
include(":convention")

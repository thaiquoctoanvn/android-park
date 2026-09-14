plugins {
    id("androidpark.android.application")
    id("androidpark.android.hilt")
    alias(libs.plugins.gms.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "io.edenx.androidpark"

    signingConfigs {
        create("release") {
            storeFile = rootProject.file("signing.jks")
            storePassword = "toanthai"
            keyAlias = "signing"
            keyPassword = "toanthai"
        }
    }

    defaultConfig {
        applicationId = "io.edenx.androidpark"
        versionCode = 7
        versionName = "1.1.0"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            manifestPlaceholders["appName"] = "Andrd Playgrnd"
            buildConfigField("String", "APP_NAME", "\"Andrd Playgrnd\"")
        }

        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            manifestPlaceholders["appName"] = "Andrd Playgrnd"
            buildConfigField("String", "APP_NAME", "\"Andrd Playgrnd\"")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":core:analytics"))
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))

    // The registry in TypeMenu is the only thing that knows every sample.
    implementation(project(":feature:animation"))
    implementation(project(":feature:backstack"))
    implementation(project(":feature:billing"))
    implementation(project(":feature:camera"))
    implementation(project(":feature:connectivity"))
    implementation(project(":feature:gesture"))
    implementation(project(":feature:googlepay"))
    implementation(project(":feature:media"))
    implementation(project(":feature:nav"))
    implementation(project(":feature:paging"))
    implementation(project(":feature:webview"))

    implementation(libs.bundles.androidx.base)
    implementation(libs.bundles.lifecycle)
    implementation(libs.androidx.lifecycle.process)   // ProcessLifecycleOwner in App

    // Needed by the crashlytics Gradle plugin applied above.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)

    implementation(libs.play.app.update.ktx)          // in-app update in MenuActivity
    implementation(libs.glide)                        // menu tiles

    testImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}

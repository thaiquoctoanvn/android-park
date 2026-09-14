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
    implementation(project(":core:network"))
    implementation(project(":core:ui"))

    implementation(project(":feature:webview"))
    implementation(project(":feature:googlepay"))
    implementation(project(":feature:billing"))
    implementation(project(":feature:paging"))
    implementation(project(":feature:camera"))
    implementation(project(":feature:media"))
    implementation(project(":feature:nav"))
    implementation(project(":feature:connectivity"))
    implementation(project(":feature:animation"))
    implementation(project(":feature:backstack"))
    implementation(project(":feature:gesture"))

    implementation(libs.bundles.androidx.base)
    implementation(libs.bundles.lifecycle)
    implementation(libs.androidx.lifecycle.process)

    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    implementation(libs.play.services.wallet)
    implementation(libs.play.app.update.ktx)
    implementation(libs.billing.ktx)

    implementation(libs.bundles.camerax)
    implementation(libs.bundles.mlkit)
    implementation(libs.zxing.core)

    implementation(libs.bundles.media3)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.bundles.navigation)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.glide)

    testImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}

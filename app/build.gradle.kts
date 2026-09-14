plugins {
    id("androidpark.android.application")
    id("androidpark.android.hilt")
    alias(libs.plugins.gms.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    // Must stay io.edenx.androidplayground for now: every source file still
    // lives in that package. The module split renames it to io.edenx.androidpark.
    namespace = "io.edenx.androidplayground"

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
    implementation(libs.bundles.androidx.base)
    implementation(libs.bundles.lifecycle)
    implementation(libs.androidx.lifecycle.process)

    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    implementation(libs.play.services.ads)
    implementation(libs.play.services.wallet)
    implementation(libs.play.app.update.ktx)
    implementation(libs.billing.ktx)

    implementation(libs.bundles.camerax)
    implementation(libs.bundles.mlkit)
    implementation(libs.zxing.core)

    implementation(libs.bundles.media3)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.bundles.navigation)

    implementation(libs.bundles.retrofit)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.glide)

    testImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}

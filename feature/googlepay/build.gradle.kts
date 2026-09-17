plugins {
    id("androidpark.android.feature")
    id("androidpark.android.view")
    id("androidpark.android.hilt")
}

android {
    namespace = "io.edenx.androidpark.feature.googlepay"
}

dependencies {
    implementation(libs.play.services.wallet)
}

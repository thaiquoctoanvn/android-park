plugins {
    id("androidpark.android.feature")
    id("androidpark.android.view")
}

android {
    namespace = "io.edenx.androidpark.feature.nav"
}

dependencies {
    implementation(libs.bundles.navigation)
    implementation(libs.glide)
}

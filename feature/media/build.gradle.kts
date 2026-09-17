plugins {
    id("androidpark.android.feature")
    id("androidpark.android.view")
}

android {
    namespace = "io.edenx.androidpark.feature.media"
}

dependencies {
    implementation(libs.bundles.media3)
    implementation(libs.glide)
}

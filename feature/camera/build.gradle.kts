plugins {
    id("androidpark.android.feature")
    id("androidpark.android.view")
}

android {
    namespace = "io.edenx.androidpark.feature.camera"
}

dependencies {
    implementation(libs.bundles.camerax)
    implementation(libs.bundles.mlkit)
    implementation(libs.glide)
    implementation(libs.gson)
}

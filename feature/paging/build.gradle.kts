plugins {
    id("androidpark.android.feature")
    id("androidpark.android.view")
    id("androidpark.android.hilt")
}

android {
    namespace = "io.edenx.androidpark.feature.paging"
}

dependencies {
    implementation(project(":core:network"))
    implementation(libs.androidx.paging.runtime)
    implementation(libs.glide)
}

plugins {
    id("androidpark.android.library")
    id("androidpark.android.hilt")
}

android {
    namespace = "io.edenx.androidpark.core.analytics"
}

dependencies {
    implementation(project(":core:common"))

    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)
    api(libs.play.services.ads)
}

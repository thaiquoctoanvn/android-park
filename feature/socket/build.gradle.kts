plugins {
    id("androidpark.android.feature")
    id("androidpark.android.compose")
    id("androidpark.android.hilt")
}

android {
    namespace = "io.edenx.androidpark.feature.socket"
}

dependencies {
    implementation(libs.okhttp)
    implementation(libs.okhttp.mockwebserver)   // the fake ticker server runs in-process
    implementation(libs.gson)

    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}

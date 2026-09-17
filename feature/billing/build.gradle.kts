plugins {
    id("androidpark.android.feature")
    id("androidpark.android.view")
    id("androidpark.android.hilt")
}

android {
    namespace = "io.edenx.androidpark.feature.billing"
}

dependencies {
    implementation(project(":core:analytics"))
    implementation(libs.billing.ktx)
    implementation(libs.gson)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config)
}

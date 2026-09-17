plugins {
    id("androidpark.android.feature")
    id("androidpark.android.view")
}

android {
    namespace = "io.edenx.androidpark.feature.connectivity"
}

dependencies {
    implementation(libs.zxing.core)   // QR encoding in FileTransferActivity
    implementation(libs.gson)
}

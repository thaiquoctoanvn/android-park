plugins {
    id("androidpark.android.library")
    id("androidpark.android.compose")
}

android {
    namespace = "io.edenx.androidpark.core.designsystem"
}

dependencies {
    // `api` so every consumer of the theme also gets the widgets it styles.
    api(libs.androidx.appcompat)
    api(libs.material)
}

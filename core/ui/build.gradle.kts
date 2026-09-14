plugins {
    id("androidpark.android.library")
}

android {
    namespace = "io.edenx.androidpark.core.ui"

    // Set directly rather than through androidpark.android.view: that
    // plugin also adds api(:core:ui), which this module cannot depend on.
    buildFeatures { viewBinding = true }
}

dependencies {
    // `api` so every feature depending on :core:ui also sees the theme in XML
    // and the DesignSystemR alias in Kotlin.
    api(project(":core:designsystem"))
    implementation(project(":core:common"))

    // BaseActivity<VB : ViewBinding> and BaseFragment<VB : ViewBinding> expose
    // these types in their public signatures, so they must be `api`.
    api(libs.androidx.appcompat)
    api(libs.androidx.fragment.ktx)
    api(libs.androidx.recyclerview)

    implementation(libs.androidx.activity.ktx)
    implementation(libs.bundles.lifecycle)
}

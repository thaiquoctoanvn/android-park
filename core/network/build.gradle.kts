plugins {
    id("androidpark.android.library")
    id("androidpark.android.hilt")
}

android {
    namespace = "io.edenx.androidpark.core.network"
}

dependencies {
    // `api` because DogApi returns core:model types and consumers call it
    // through Retrofit types.
    api(project(":core:model"))
    api(libs.bundles.retrofit)
}

plugins {
    id("androidpark.jvm.library")
}

dependencies {
    implementation(libs.gson)            // @SerializedName
    implementation(libs.androidx.annotation)  // @Keep - a plain jar, JVM-safe
}

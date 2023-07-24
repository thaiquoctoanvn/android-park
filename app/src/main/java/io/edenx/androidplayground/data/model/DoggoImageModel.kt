package io.edenx.androidplayground.data.model


import com.google.gson.annotations.SerializedName

data class DoggoImageModel(
    @SerializedName("breeds")
    val breeds: List<Any?>? = null,
    @SerializedName("categories")
    val categories: List<Any?>? = null,
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("url")
    val url: String? = null
)

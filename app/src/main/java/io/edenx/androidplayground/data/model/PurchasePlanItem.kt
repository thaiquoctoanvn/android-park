package io.edenx.androidplayground.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class PurchasePlanItem(
    @SerializedName("id")
    val id: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("price")
    val price: String? = "$1",
    @SerializedName("type")
    val type: String? = "",
    @SerializedName("isSelected")
    var isSelected: Boolean? = false,
    @SerializedName("description")
    val description: String? = ""
)

@Keep
data class SimpleProductItem(
    @SerializedName("products")
    val products: List<PurchasePlanItem>?
)

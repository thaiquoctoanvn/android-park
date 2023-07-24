package io.edenx.androidplayground.data.model

data class PurchasePlanItem(
    val id: String?,
    val title: String?,
    val price: String? = "$1",
    val type: String? = "",
    var isSelected: Boolean? = false
)

data class SimpleProductItem(
    val products: List<PurchasePlanItem>?
)

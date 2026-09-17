package io.edenx.androidpark.menu


data class MenuItem(
    val id: Int,
    val type: TypeMenu = TypeMenu.BILLING,
    val name: String = "",
    val hint: String = "",
    var iconRes: Int? = null,
    var bgColor: Int? = null
)
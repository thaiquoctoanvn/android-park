package io.edenx.androidplayground.data.model

import io.edenx.androidplayground.data.TypeMenu

data class MenuItem(
    val id: Int,
    val type: TypeMenu = TypeMenu.BILLING,
    val name: String = "",
    val hint: String = "",
    var iconRes: Int? = null,
    var bgColor: Int? = null
)
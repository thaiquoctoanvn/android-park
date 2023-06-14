package io.edenx.androidpark.data.model

data class MenuItem(
    val id: Int,
    val type: TypeMenu = TypeMenu.BILLING,
    val name: String = "",
    val hint: String = "",
    var iconRes: Int? = null
)

enum class TypeMenu(val prompt: String) {
    BILLING("Inapp billing"),
    PAGING("Paging 3"),
    NAV("Navigation"),
    ANIMATION("Animation"),
    IMG_LABELING("Img Labeling"),
    QR_DETECTING("Qr Detecting"),
    FILE_TRANSFERRING("File Transferring"),
    BLUETOOTH_DISCOVERY("Bluetooth Discovery")
}

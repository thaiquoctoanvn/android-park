package io.edenx.androidplayground.data.model

import io.edenx.androidplayground.component.animation.AnimationActivity
import io.edenx.androidplayground.component.camera.CameraActivity
import io.edenx.androidplayground.component.connectivity.BluetoothConnectingActivity
import io.edenx.androidplayground.component.connectivity.FileTransferActivity
import io.edenx.androidplayground.component.nav.NavigationActivity
import io.edenx.androidplayground.component.paging.PagingActivity

data class MenuItem(
    val id: Int,
    val type: TypeMenu = TypeMenu.BILLING,
    val name: String = "",
    val hint: String = "",
    var iconRes: Int? = null
)

enum class TypeMenu(val prompt: String, val screen: Class<*>? = null) {
    BILLING("Inapp billing"),
    PAGING("Paging 3", PagingActivity::class.java),
    NAV("Navigation", NavigationActivity::class.java),
    ANIMATION("Animation", AnimationActivity::class.java),
    IMG_LABELING("Img Labeling", CameraActivity::class.java),
    QR_DETECTING("Qr Detecting", CameraActivity::class.java),
    FILE_TRANSFERRING("File Transferring", FileTransferActivity::class.java),
    BLUETOOTH_DISCOVERY("Bluetooth Discovery", BluetoothConnectingActivity::class.java)
}

package io.edenx.androidplayground.data

import io.edenx.androidplayground.component.animation.AnimationActivity
import io.edenx.androidplayground.component.backstack.BackStackActivity
import io.edenx.androidplayground.component.billing.PurchaseActivity
import io.edenx.androidplayground.component.camera.CameraActivity
import io.edenx.androidplayground.component.connectivity.BluetoothConnectingActivity
import io.edenx.androidplayground.component.connectivity.FileTransferActivity
import io.edenx.androidplayground.component.media.PlaylistActivity
import io.edenx.androidplayground.component.nav.NavigationActivity
import io.edenx.androidplayground.component.paging.PagingActivity

enum class TypeMenu(val prompt: String, val screen: Class<*>? = null) {
    BILLING("Inapp billing", PurchaseActivity::class.java),
    PAGING("Paging 3", PagingActivity::class.java),
    NAV("Navigation", NavigationActivity::class.java),
    ANIMATION("Animation", AnimationActivity::class.java),
    IMG_LABELING("Img Labeling", CameraActivity::class.java),
    QR_DETECTING("Qr Detecting", CameraActivity::class.java),
    FILE_TRANSFERRING("File Transferring", FileTransferActivity::class.java),
    BLUETOOTH_DISCOVERY("Bluetooth Discovery", BluetoothConnectingActivity::class.java),
    MEDIA_3("Media Player", PlaylistActivity::class.java),
    LAUNCH_MODE("Launch Mode", BackStackActivity::class.java)
}

enum class ActivityLaunchFlag {
    SINGLE_TOP,
    NEW_TASK,
    CLEAR_TOP,
}
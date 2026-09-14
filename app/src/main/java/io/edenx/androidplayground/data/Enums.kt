package io.edenx.androidplayground.data

import io.edenx.androidpark.feature.animation.AnimationActivity
import io.edenx.androidpark.feature.backstack.BackStackActivity
import io.edenx.androidpark.feature.billing.PurchaseActivity
import io.edenx.androidpark.feature.camera.CameraActivity
import io.edenx.androidpark.feature.connectivity.BluetoothConnectingActivity
import io.edenx.androidpark.feature.connectivity.FileTransferActivity
import io.edenx.androidpark.feature.media.PlaylistActivity
import io.edenx.androidpark.feature.nav.NavigationActivity
import io.edenx.androidpark.feature.paging.PagingActivity
import io.edenx.androidpark.feature.googlepay.GooglePayActivity

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
    LAUNCH_MODE("Launch Mode", BackStackActivity::class.java),
    GOOGLE_PAY("Google Pay", GooglePayActivity::class.java)
}

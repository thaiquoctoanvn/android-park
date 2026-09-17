package io.edenx.androidpark.feature.camera

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.commit
import io.edenx.androidpark.core.ui.BaseActivity
import io.edenx.androidpark.feature.camera.databinding.ActivityCameraBinding
import io.edenx.androidpark.core.designsystem.R as DesignSystemR

class CameraActivity : BaseActivity<ActivityCameraBinding>(ActivityCameraBinding::inflate) {
    override fun onViewCreated() {
        WindowCompat.getInsetsController(window, binding.root).hide(WindowInsetsCompat.Type.statusBars())
        supportFragmentManager.commit {
            setCustomAnimations(DesignSystemR.anim.slide_from_right, 0, 0, DesignSystemR.anim.slide_to_right)
            when (CameraNavigation.typeOf(intent)) {
                CameraScreenType.IMG_LABELING -> add(binding.fcv.id, ImgLabelingFragment::class.java, null)
                CameraScreenType.QR_DETECTING -> add(binding.fcv.id, QrDetectingFragment::class.java, null)
                null -> Unit
            }
        }
    }
}
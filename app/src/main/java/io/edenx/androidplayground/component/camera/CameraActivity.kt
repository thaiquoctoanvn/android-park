package io.edenx.androidplayground.component.camera

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
import io.edenx.androidplayground.R
import io.edenx.androidpark.core.ui.BaseActivity
import io.edenx.androidplayground.databinding.ActivityCameraBinding
import io.edenx.androidpark.core.designsystem.R as DesignSystemR

class CameraActivity : BaseActivity<ActivityCameraBinding>(ActivityCameraBinding::inflate) {
    override fun onViewCreated() {
        WindowCompat.getInsetsController(window, binding.root).hide(WindowInsetsCompat.Type.statusBars())
        supportFragmentManager.commit {
            setCustomAnimations(DesignSystemR.anim.slide_from_right, 0, 0, DesignSystemR.anim.slide_to_right)
            when (intent.getStringExtra("type")) {
                CameraScreenType.IMG_LABELING.name -> add(binding.fcv.id, ImgLabelingFragment::class.java, null)
                CameraScreenType.QR_DETECTING.name -> add(binding.fcv.id, QrDetectingFragment::class.java, null)
            }
        }
    }

    enum class CameraScreenType {
        IMG_LABELING, QR_DETECTING
    }
}
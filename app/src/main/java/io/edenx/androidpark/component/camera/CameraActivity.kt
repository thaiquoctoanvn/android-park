package io.edenx.androidpark.component.camera

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.commit
import io.edenx.androidpark.R
import io.edenx.androidpark.component.animation.AddToCartFragment
import io.edenx.androidpark.component.base.BaseActivity
import io.edenx.androidpark.databinding.ActivityCameraBinding

class CameraActivity : BaseActivity<ActivityCameraBinding>(ActivityCameraBinding::inflate) {
    override fun onViewCreated() {
        supportFragmentManager.commit {
            setCustomAnimations(R.anim.slide_from_right, 0, 0, R.anim.slide_to_right)
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
package io.edenx.androidpark.feature.animation

import androidx.fragment.app.commit
import io.edenx.androidpark.core.ui.BaseActivity
import io.edenx.androidpark.feature.animation.databinding.ActivityAnimationBinding
import io.edenx.androidpark.core.designsystem.R as DesignSystemR

class AnimationActivity : BaseActivity<ActivityAnimationBinding>(ActivityAnimationBinding::inflate) {
    override fun onViewCreated() {
        supportFragmentManager.commit {
            setCustomAnimations(DesignSystemR.anim.slide_from_right, 0, 0, DesignSystemR.anim.slide_to_right)
            add(binding.fcv.id, AddToCartFragment::class.java, null)
        }
    }
}
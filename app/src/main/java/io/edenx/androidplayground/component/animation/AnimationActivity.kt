package io.edenx.androidplayground.component.animation

import androidx.fragment.app.commit
import io.edenx.androidplayground.R
import io.edenx.androidplayground.component.base.BaseActivity
import io.edenx.androidplayground.databinding.ActivityAnimationBinding
import io.edenx.androidpark.core.designsystem.R as DesignSystemR

class AnimationActivity : BaseActivity<ActivityAnimationBinding>(ActivityAnimationBinding::inflate) {
    override fun onViewCreated() {
        supportFragmentManager.commit {
            setCustomAnimations(DesignSystemR.anim.slide_from_right, 0, 0, DesignSystemR.anim.slide_to_right)
            add(binding.fcv.id, AddToCartFragment::class.java, null)
        }
    }
}
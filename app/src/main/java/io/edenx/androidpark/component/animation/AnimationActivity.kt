package io.edenx.androidpark.component.animation

import androidx.fragment.app.commit
import io.edenx.androidpark.R
import io.edenx.androidpark.component.base.BaseActivity
import io.edenx.androidpark.databinding.ActivityAnimationBinding

class AnimationActivity : BaseActivity<ActivityAnimationBinding>(ActivityAnimationBinding::inflate) {
    override fun onViewCreated() {
        supportFragmentManager.commit {
            setCustomAnimations(R.anim.slide_from_right, 0, 0, R.anim.slide_to_right)
            add(binding.fcv.id, AddToCartFragment::class.java, null)
        }
    }
}
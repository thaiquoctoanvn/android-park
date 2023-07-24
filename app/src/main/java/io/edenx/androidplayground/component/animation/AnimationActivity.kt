package io.edenx.androidplayground.component.animation

import androidx.fragment.app.commit
import io.edenx.androidplayground.R
import io.edenx.androidplayground.component.base.BaseActivity
import io.edenx.androidplayground.databinding.ActivityAnimationBinding

class AnimationActivity : BaseActivity<ActivityAnimationBinding>(ActivityAnimationBinding::inflate) {
    override fun onViewCreated() {
        supportFragmentManager.commit {
            setCustomAnimations(R.anim.slide_from_right, 0, 0, R.anim.slide_to_right)
            add(binding.fcv.id, AddToCartFragment::class.java, null)
        }
    }
}
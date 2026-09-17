package io.edenx.androidpark.feature.animation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.edenx.androidpark.core.ui.BaseFragment
import io.edenx.androidpark.feature.animation.databinding.FragmentAddToCartBinding
import io.edenx.androidplayground.util.AnimationUtil

class AddToCartFragment : BaseFragment<FragmentAddToCartBinding>(FragmentAddToCartBinding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun setListener() {
        binding.ibIncrease.setOnClickListener {
            AnimationUtil.AddToCartAnimation()
                .setTargetView(binding.ivMealPreview)
                .setDestView(binding.clCartButtonContainer)
                .setMoveDuration(2000)
                .attachActivity(requireActivity())
                .startAnimation()
        }
    }
}
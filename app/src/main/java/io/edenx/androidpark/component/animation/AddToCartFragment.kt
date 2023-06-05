package io.edenx.androidpark.component.animation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.edenx.androidpark.R
import io.edenx.androidpark.component.base.BaseFragment
import io.edenx.androidpark.databinding.FragmentAddToCartBinding
import io.edenx.androidpark.util.AnimationUtil

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
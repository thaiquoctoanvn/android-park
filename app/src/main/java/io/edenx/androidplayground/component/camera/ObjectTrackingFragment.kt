package io.edenx.androidplayground.component.camera

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.edenx.androidplayground.R
import io.edenx.androidplayground.component.base.BaseFragment
import io.edenx.androidplayground.databinding.FragmentObjectTrackingBinding

class ObjectTrackingFragment :
    BaseFragment<FragmentObjectTrackingBinding>(FragmentObjectTrackingBinding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
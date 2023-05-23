package io.lacanh.aiassistant.component.nav

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.lacanh.aiassistant.R
import io.lacanh.aiassistant.component.base.BaseFragment
import io.lacanh.aiassistant.databinding.FragmentFirstBinding

class FirstFragment : BaseFragment<FragmentFirstBinding>(FragmentFirstBinding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.np.apply {
            displayedValues = arrayOf("00", "15", "30", "45")
        }
    }

    override fun setListener() {
        binding.btNav.setOnClickListener {

        }
    }
}
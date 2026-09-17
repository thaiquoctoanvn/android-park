package io.edenx.androidpark.feature.nav

import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import io.edenx.androidpark.core.ui.BaseActivity
import io.edenx.androidpark.feature.nav.databinding.ActivityNavigationBinding

class NavigationActivity : BaseActivity<ActivityNavigationBinding>(ActivityNavigationBinding::inflate) {
    override fun onViewCreated() {
        supportFragmentManager.findFragmentById(binding.navHostFragment.id)?.findNavController()?.let {
            binding.bnv.setupWithNavController(it)
        }
    }
}
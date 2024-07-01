package io.edenx.androidplayground.component.nav

import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import io.edenx.androidplayground.component.base.BaseActivity
import io.edenx.androidplayground.databinding.ActivityNavigationBinding

@AndroidEntryPoint
class NavigationActivity : BaseActivity<ActivityNavigationBinding>(ActivityNavigationBinding::inflate) {
    override fun onViewCreated() {
        supportFragmentManager.findFragmentById(binding.navHostFragment.id)?.findNavController()?.let {
            binding.bnv.setupWithNavController(it)
        }
    }
}
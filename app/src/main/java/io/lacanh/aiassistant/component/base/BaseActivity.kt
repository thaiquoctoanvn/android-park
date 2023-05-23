package io.lacanh.aiassistant.component.base

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import io.lacanh.aiassistant.R

typealias ViewBindingType<T> = (LayoutInflater) -> T

abstract class BaseActivity<VB : ViewBinding>(
    private val inflate: ViewBindingType<VB>
) : AppCompatActivity() {

    private var _binding: VB? = null
    val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        window.navigationBarColor = Color.WHITE
        super.onCreate(savedInstanceState)
        _binding = inflate.invoke(layoutInflater)
        setContentView(binding.root)
        setObserver()
        onViewCreated()
        setListener()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    abstract fun onViewCreated()
    open fun setListener() = Unit
    open fun setObserver() = Unit
}
package io.edenx.androidpark.component.base

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import io.edenx.androidpark.R

typealias ViewBindingType<T> = (LayoutInflater) -> T

abstract class BaseActivity<VB : ViewBinding>(
    private val inflate: ViewBindingType<VB>
) : AppCompatActivity() {

    private var _binding: VB? = null
    val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        //window.navigationBarColor = Color.WHITE
        super.onCreate(savedInstanceState)
        _binding = inflate.invoke(layoutInflater)
        setContentView(binding.root)
        setObserver()
        onViewCreated()
        setListener()
        observeKeyboardEvent()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            if (it.action == MotionEvent.ACTION_UP)
                ViewCompat.getRootWindowInsets(binding.root)?.let { rootView ->
                    // Keyboard has been opening
                    currentFocus?.let { view ->
                        if (rootView.isVisible(WindowInsetsCompat.Type.ime())) {
                            // close keyboard
                        }
                    }


                }
        }
        return super.dispatchTouchEvent(event)
    }

    // Keyboard shows or hides
    private fun observeKeyboardEvent() {
        window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
            val isKeyboardVisible = WindowInsetsCompat.toWindowInsetsCompat(windowInsets, view)
            if (isKeyboardVisible.isVisible(WindowInsetsCompat.Type.ime())) {

            } else {

            }
            view.onApplyWindowInsets(windowInsets)
        }
    }

    abstract fun onViewCreated()
    open fun setListener() = Unit
    open fun setObserver() = Unit
}
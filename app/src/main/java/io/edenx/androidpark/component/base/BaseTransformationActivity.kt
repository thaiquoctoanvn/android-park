package io.edenx.androidpark.component.base
//
//import android.graphics.Color
//import android.os.Bundle
//import android.view.LayoutInflater
//import androidx.viewbinding.ViewBinding
//import com.skydoves.transformationlayout.TransformationAppCompatActivity
//
//typealias TransformationViewBindingType<T> = (LayoutInflater) -> T
//
//abstract class BaseTransformationActivity<VB : ViewBinding>(
//    private val inflate: TransformationViewBindingType<VB>
//) : TransformationAppCompatActivity() {
//    private var _binding: VB? = null
//    val binding get() = _binding!!
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        window.navigationBarColor = Color.WHITE
//        super.onCreate(savedInstanceState)
//        _binding = inflate.invoke(layoutInflater)
//        setContentView(binding.root)
//        setObserver()
//        onViewCreated()
//        setListener()
//    }
//
//    override fun onBackPressed() {
//        super.onBackPressed()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        _binding = null
//    }
//
//    abstract fun onViewCreated()
//    open fun setListener() = Unit
//    open fun setObserver() = Unit
//}
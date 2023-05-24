package io.lacanh.aiassistant.component.gesture

import android.annotation.SuppressLint
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View
import io.lacanh.aiassistant.component.base.BaseActivity
import io.lacanh.aiassistant.data.model.TouchPointItem
import io.lacanh.aiassistant.databinding.ActivityMultiTouchBinding

class MultiTouchActivity : BaseActivity<ActivityMultiTouchBinding>(ActivityMultiTouchBinding::inflate) {

    private val touchPoints = mutableListOf<TouchPointItem>()

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated() {
        binding.v.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action and motionEvent.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    touchPoints.add(TouchPointItem(motionEvent.x, motionEvent.y))
                    Log.d("xxxx", "Touch points: $touchPoints")
                    //changeTouchArea(activityBinding.vTargetTouch)
                }
                else -> return@setOnTouchListener false
            }
            return@setOnTouchListener true
        }
    }

    private fun changeTouchArea(view: View) {
        binding.rootLayout.post {
            val touchArea = Rect()
            view.getHitRect(touchArea)
            touchArea.apply {
                top -= 600
                bottom += 600
                left -= 600
                right += 600
            }
            binding.rootLayout.touchDelegate = TouchDelegate(touchArea, view)
        }
    }
}
package io.edenx.androidpark.ext

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import io.edenx.androidpark.R

fun View.setOnSoundClickListener(
    debounceTime: Long = 800L,
    onClick: (view: View) -> Unit,
    onSoundPlayedOnClick: (() -> Unit)?
) {
    var lastClickTime = 0L
    this.setOnClickListener {
        if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime)
            return@setOnClickListener
        else {
            if (onSoundPlayedOnClick != null) onSoundPlayedOnClick()
            onClick(it)
        }
        lastClickTime = SystemClock.elapsedRealtime()
    }
}

fun AppCompatActivity.startActivityWithTransition(
    intent: Intent,
    bundle: Bundle = bundleOf()
) {
    startActivity(intent, bundle)
    overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
}

fun AppCompatActivity.closeKeyboard(view: View) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        view.windowInsetsController?.hide(WindowInsetsCompat.Type.ime())
    } else {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

fun AppCompatActivity.openKeyboard(view: View) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        view.windowInsetsController?.show(WindowInsetsCompat.Type.ime())
    } else {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun RecyclerView.snapSmoothToPosition(position: Int, snapMode: Int = LinearSmoothScroller.SNAP_TO_START) {
    if (position >= 0) {
        val smoothScroller = object : LinearSmoothScroller(this.context) {
            override fun getVerticalSnapPreference(): Int = snapMode
            override fun getHorizontalSnapPreference(): Int = snapMode
        }
        smoothScroller.targetPosition = position
        layoutManager?.startSmoothScroll(smoothScroller)
    }
}
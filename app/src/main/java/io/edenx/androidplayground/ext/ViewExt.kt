package io.edenx.androidplayground.ext

import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import io.edenx.androidplayground.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.UUID

fun ComponentActivity.startActivityWithTransition(
    intent: Intent,
    bundle: Bundle = bundleOf()
) {
    startActivity(intent, bundle)
    overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
}

fun ComponentActivity.closeKeyboard(view: View) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        view.windowInsetsController?.hide(WindowInsetsCompat.Type.ime())
    } else {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

fun ComponentActivity.openKeyboard(view: View) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        view.windowInsetsController?.show(WindowInsetsCompat.Type.ime())
    } else {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun ComponentActivity.requestMultiplePermissions(permissions: Array<String>, then: (Map<String, Boolean>) -> Unit) {
    if (permissions.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }) then(
        permissions.associateWith { true })
    else CoroutineScope(Dispatchers.Main).launch {
        combine(flows = arrayOf(
            callbackFlow<Map<String, Boolean>> {
                val multiplePermissionRequest = activityResultRegistry.register(
                    UUID.randomUUID().toString(), ActivityResultContracts.RequestMultiplePermissions()) {
                    trySend(it)
                }
                multiplePermissionRequest.launch(permissions)
                awaitClose {
                    multiplePermissionRequest.unregister()
                }
            }
        ), transform = {
            then(it.first())
        }).collect()
    }
}

fun ComponentActivity.launchImagePicker(
    isAllowedMultiplePick: Boolean = false,
    onCancel: () -> Unit = {},
    onPick: (Intent?) -> Unit
) {
    CoroutineScope(Dispatchers.Main).launch {
        combine(flows = arrayOf(
            callbackFlow<ActivityResult> {
                val launcher = activityResultRegistry.register(
                    UUID.randomUUID().toString(), ActivityResultContracts.StartActivityForResult()) {
                    trySend(it)
                }
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                if (isAllowedMultiplePick) intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                launcher.launch(Intent.createChooser(intent, ""))
                awaitClose {
                    launcher.unregister()
                }
            }
        ), transform = {
            it.first().let {
                if (it.resultCode == RESULT_OK) {
                    // For multiple pick
                    // intent.data.clipData.getItemAt
                    // For one pick
                    // intent.data
                    onPick(it.data)
                } else if (it.resultCode == RESULT_CANCELED) onCancel()
            }
        }).collect()
    }
}

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
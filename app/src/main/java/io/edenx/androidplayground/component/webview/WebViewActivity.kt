package io.edenx.androidplayground.component.webview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.Toast
import io.edenx.androidplayground.component.base.BaseActivity
import io.edenx.androidplayground.databinding.ActivityWebViewBinding

class WebViewActivity : BaseActivity<ActivityWebViewBinding>(ActivityWebViewBinding::inflate) {
    override fun onViewCreated() {
        setupWebView()
    }

    private fun setupWebView() {
        binding.wv.apply {
            settings.javaScriptEnabled = true
            webViewClient = CustomWebViewClient(
                mOnPageStarted = { _, _, _ ->

                },
                mOnPageFinished = { _, _ ->

                },
                mOnReceivedError = { _, _, _ ->

                }
            )

            // Disable device vibration
            //isHapticFeedbackEnabled = false
            isLongClickable = true
            // Show copy/paste for long click in case the element of web page is an edit text box
            setOnLongClickListener {
                if (hitTestResult.type == WebView.HitTestResult.EDIT_TEXT_TYPE)
                    return@setOnLongClickListener false
                return@setOnLongClickListener true
            }
        }
        binding.wv.loadUrl("https://translate.google.com")
    }
}
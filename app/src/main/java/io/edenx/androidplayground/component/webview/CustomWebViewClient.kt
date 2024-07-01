package io.edenx.androidplayground.component.webview

import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class CustomWebViewClient(
    private val mOnPageStarted: ((WebView?, String?, Bitmap?) -> Unit) = { _, _, _ -> },
    private val mOnPageFinished: ((WebView?, String?) -> Unit) = { _, _ -> },
    private val mOnReceivedError: ((WebView?, WebResourceRequest?, WebResourceError?) -> Unit) = { _, _, _ -> }
) : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        /*
        * true -> cancel current load
        * */
        request?.url?.let {
            if (Uri.parse(it.toString()).host == "https://translate.google.com") {
                return false
            }
        }

        return true
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        mOnPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        mOnPageFinished(view, url)
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        mOnReceivedError(view, request, error)
    }
}
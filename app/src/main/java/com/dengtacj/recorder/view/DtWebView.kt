package com.dengtacj.recorder.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SizeUtils
import com.tencent.smtt.sdk.WebChromeClient
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient

class DtWebView(context: Context, attributeSet: AttributeSet) : WebView(context, attributeSet) {

    private var webIndicator: WebIndicator

    init {
        //启用支持javascript
        settings.javaScriptEnabled = true
        // 将图片调整到适合webview的大小
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.domStorageEnabled = true
        settings.setAppCachePath(context.cacheDir.absolutePath)
        settings.allowFileAccess = true
        settings.setAppCacheEnabled(true)
        settings.allowContentAccess = true
        settings.savePassword = false
        settings.databaseEnabled = true
        settings.saveFormData = false
        settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        webChromeClient = DtWebChromeClient()
        webViewClient = WebViewClient()

        webIndicator = WebIndicator(context)
//        webIndicator.layoutParams =
//            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(1f))
//        addView(webIndicator)

    }


    inner class DtWebChromeClient : WebChromeClient() {

        override fun onProgressChanged(WebView: WebView?, newProgress: Int) {
            super.onProgressChanged(WebView, newProgress)
            Log.i("DtWebView", "onProgressChanged: $newProgress")
//            webIndicator?.apply {
//                when (newProgress) {
//                    0 -> reset()
//                    in 1..10 -> show()
//                    in 11..94 -> setProgress(newProgress)
//                    else -> {
//                        setProgress(newProgress)
//                        hide()
//                    }
//                }
//            }
        }

    }


}
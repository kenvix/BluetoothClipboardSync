package com.kenvix.utils.android

import android.annotation.SuppressLint
import android.view.KeyEvent
import android.webkit.WebSettings
import android.webkit.WebView
import com.kenvix.clipboardsync.ApplicationEnvironment

/**
 * Webview 初始化器
 * @param webView 需要初始化的 webview
 */
class WebViewInitializer(private val webView: WebView) {
    private var extendUserAgent = "Kenvix Generic Android Client"

    /**
     * 初始化一般 webview 设置
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun initDefaultWebSettings() {
        val webSettings = webView.settings

        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        webSettings.useWideViewPort = true //将图片调整到适合webview的大小
        webSettings.loadWithOverviewMode = true // 缩放至屏幕的大小
        webSettings.defaultTextEncodingName = "utf-8"//设置编码格式

        //允许js代码
        webSettings.javaScriptEnabled = true
        //允许SessionStorage/LocalStorage存储
        webSettings.domStorageEnabled = true

        //允许缓存，设置缓存位置
        webSettings.setAppCacheEnabled(true)
        webSettings.setAppCachePath(ApplicationEnvironment.appContext.cacheDir.path)

        //允许WebView使用File协议
        webSettings.allowFileAccess = true

        //设置UA
        webSettings.userAgentString = webSettings.userAgentString + extendUserAgent

        //自动加载图片
        webSettings.loadsImagesAutomatically = true
    }

    /**
     * 禁用放缩
     */
    fun disableZoom() {
        val webSettings = webView.settings

        //禁用放缩
        webSettings.displayZoomControls = false
        webSettings.builtInZoomControls = false
        //禁用文字缩放
        webSettings.textZoom = 100
    }

    fun enableFirstLoadAnime() {
        //TODO: Loading Anime
    }

    /**
     * 让 webview 加载指定页面
     * @param url 页面地址
     */
    fun loadUrl(url: String) {
        webView.loadUrl(url)
    }

    /**
     * 按常见的策略来初始化 webview
     * @param url 页面地址
     */
    @JvmOverloads
    fun setupWithCommonConfig(url: String? = null) {
        enableFirstLoadAnime()
        initDefaultWebSettings()
        disableZoom()

        if (url != null)
            loadUrl(url)
    }

    /**
     * 处理按键被按下时 webview 的事件
     * 一定要用它，否则用户按返回会直接退出 activity
     * @param keyCode  直接传入从 activity 获得的参数
     * @param event 直接传入从 activity 获得的参数
     */
    fun onKeyDownCallback(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }

        return false
    }
}
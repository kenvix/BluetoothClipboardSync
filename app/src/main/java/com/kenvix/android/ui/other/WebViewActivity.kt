//--------------------------------------------------
// Class WebViewActivity
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.android.ui.other

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.webkit.WebView
import com.kenvix.android.ApplicationEnvironment
import com.kenvix.utils.android.annotation.AutoLoadOption
import com.kenvix.android.ui.base.BaseActivity
import com.kenvix.android.utils.WebViewInitializer
import com.kenvix.utils.android.annotation.ViewAutoLoad

@AutoLoadOption(enabled = true)
class WebViewActivity : BaseActivity() {
    @ViewAutoLoad lateinit var webViewCore: WebView
    lateinit var webViewInitializer: WebViewInitializer

    override fun onInitialize(savedInstanceState: Bundle?) {
        webViewInitializer =
            WebViewInitializer(webViewCore)
        webViewInitializer.setupWithCommonConfig(intent.getStringExtra("url"))
    }

    override fun getBaseLayout(): Int = ApplicationEnvironment.getAppResourceIdentifier("activity_webview", "layout")
    override fun getBaseContainer(): Int = ApplicationEnvironment.getAppResourceIdentifier("web_view_container", "id")

    companion object Info {
        /**
         * 启动这个 Activity
         * 按照规范，所有 Activity 都要定义这样的一个方法
         * @param fromActivity **来源** Activity 的 对象
         * @param requestCode **来源** Activity 的 Request code
         */
        @JvmOverloads
        @JvmStatic
        fun startActivity(fromActivity: Activity, requestCode: Int, url: String, title: String? = null, isTitleFollowed: Boolean = true) {
            val requestIntent = Intent(fromActivity, WebViewActivity::class.java)
            requestIntent.putExtra("url", url)
            requestIntent.putExtra("title", title)
            requestIntent.putExtra("isTitleFollowed", isTitleFollowed)
            fromActivity.startActivityForResult(requestIntent, requestCode)
        }
    }
}
package com.darley.unifound.printer.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.darley.unifound.printer.R
import com.darley.unifound.printer.data.dao.CookiesDao
import com.darley.unifound.printer.ui.login.LoginActivity
import com.darley.unifound.printer.ui.printer.UploadActivity
import com.darley.unifound.printer.utils.ActivityCollector
import com.darley.unifound.printer.utils.BaseActivity


class WebViewActivity : BaseActivity() {
    companion object {
        fun actionStart(context: Context, data: String) {
            val intent = Intent(context, WebViewActivity::class.java)
            intent.data = Uri.parse(data)
            context.startActivity(intent,
                ActivityOptions.makeSceneTransitionAnimation(context as Activity).toBundle())
        }

        private const val HOST = "http://10.135.0.139:9130/"
        private const val Client = "client/new/cprintMobile/"
        const val LOGIN_URL = "$HOST${Client}login.html"
        const val INDEX_URL = "$HOST${Client}index.html"
        const val UPLOAD_URL = "$HOST${Client}cprint.html"
        const val DOC_URL = "$HOST${Client}printDoc.html"
    }

    private lateinit var webView: WebView
    private lateinit var cookieManager: CookieManager
    private lateinit var uri: Uri
    private val printerWebViewClient = object : WebViewClient() {
        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            return when (url) {
                UPLOAD_URL -> {
                    UploadActivity.actionStart(this@WebViewActivity, "webView")
                    true
                }
                LOGIN_URL -> {
                    val intent = Intent(this@WebViewActivity, LoginActivity::class.java)
                    startActivity(intent,
                        ActivityOptions.makeSceneTransitionAnimation(this@WebViewActivity)
                            .toBundle())
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    // 适配横屏宽度
    private fun setWebViewWidth(webView: WebView, config: Configuration) {
        webView.layoutParams.width = when (config.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                (resources.displayMetrics.heightPixels / 1.5).toInt()
            }
            else -> {
                resources.displayMetrics.widthPixels
            }
        }
    }

    private fun setWebCookies(cookieManager: CookieManager, uri: Uri) {
        cookieManager.setAcceptCookie(true)
        val cookies = CookiesDao.getCookies()
        cookies.forEach {
            cookieManager.setCookie(uri.toString(), it)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        webView = findViewById(R.id.webView)
        cookieManager = CookieManager.getInstance()
        uri = intent.data!!
        setWebViewWidth(webView, resources.configuration)
        setWebCookies(cookieManager, uri)
        Log.d("WebViewActivity", "cookies: ${cookieManager.getCookie(uri.toString())}")
        webView.run {
            settings.javaScriptEnabled = true
            // 开启后 webView 内的中英文语言切换可生效
//            settings.domStorageEnabled = true
            // TODO 根据系统语言状态执行 JS 脚本切换语言，应放在 onPageFinished 中执行
            // evaluateJavascript("""localStorage.setItem("lang", "2")""", null)
            webViewClient = printerWebViewClient
            // 浏览器下载文件
            setDownloadListener { url, _, _, _, _ ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
            loadUrl("$uri")
        }
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // 适配横屏宽度
        setWebViewWidth(webView, newConfig)
    }

    override fun onBackPressed() {
        Log.d("WebViewActivity", "onBackPressed: ${webView.url == INDEX_URL}")
        if (webView.url == INDEX_URL) {
            ActivityCollector.finishAll()
        } else if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

}
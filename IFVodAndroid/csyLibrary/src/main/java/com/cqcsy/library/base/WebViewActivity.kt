package com.cqcsy.library.base

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.library.R
import com.cqcsy.library.pay.card.CardPaySuccessActivity
import com.cqcsy.library.utils.AppLanguageUtils
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.JumpUtils
import com.cqcsy.library.utils.StatusBarUtil
import kotlinx.android.synthetic.main.activity_web_view.*
import org.json.JSONObject

/**
 * 网页浏览器
 * 支持URL和html
 * 如果title传了就用传入的，没有传就用网页的title
 */
open class WebViewActivity : BaseActivity() {
    companion object {
        const val titleKey = "title"
        const val urlKey = "url"
        const val contentKey = "content"
        const val postParams = "postParams" // 传入此参数，则用post方式打开URL，并且post参数

        /**
         * @param url 链接地址
         * @param content html内容，与URL二选一
         * @param params post参数 json
         */
        fun load(context: Context, url: String, title: String? = null, params: String? = null, content: String? = null) {
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra(titleKey, title)
            intent.putExtra(urlKey, url)
            intent.putExtra(contentKey, content)
            intent.putExtra(postParams, params)
            context.startActivity(intent)
        }
    }

    var incomeTitle: String? = null
    var incomeUrl: String? = null
    var incomeContent: String? = null
    var isLoadUrl = false
    var mHeader: View? = null
    var mLeftView: View? = null
    var mTitle: TextView? = null

    lateinit var mBottomContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        mHeader = findViewById(R.id.headerContainer)
        mLeftView = findViewById(R.id.leftImage)
        mTitle = findViewById(R.id.headerTitle)
        mBottomContainer = findViewById(R.id.bottom_container)
        mLeftView?.setOnClickListener {
            finish()
        }
        init()
    }

    fun init() {
        initWebView()
        incomeTitle = intent.getStringExtra(titleKey)
        incomeUrl = intent.getStringExtra(urlKey)
        incomeContent = intent.getStringExtra(contentKey)
        if (!incomeTitle.isNullOrEmpty()) {
            mTitle?.text = incomeTitle!!
        }
        if (!incomeUrl.isNullOrEmpty()) {
            val statusBarHeight = StatusBarUtil.getStatusBarHeight(this)
            if (isHideHeader(incomeUrl!!)) {
                mHeader?.isVisible = false
                StatusBarUtil.setTranslucentForImageViewInFragment(this, 0, null)
                StatusBarUtil.setLightMode(this)
                val layoutParams = webView.layoutParams as LinearLayout.LayoutParams
                layoutParams.topMargin = statusBarHeight
                webView.layoutParams = layoutParams
            }
            val language = AppLanguageUtils.getAppliedLanguage()
            if (language != null) {
                incomeUrl = Uri.parse(incomeUrl).buildUpon().appendQueryParameter("language", language.toLanguageTag()).toString()
            }
            if (isNeedLogin(incomeUrl!!) && !GlobalValue.checkLogin()) {
                isLoadUrl = false
                return
            }
            if (isNeedLogin(incomeUrl!!)) {
                incomeUrl = appendLoginInfo(incomeUrl!!)
            }
            // 添加状态栏高度
            incomeUrl =
                Uri.parse(incomeUrl).buildUpon().appendQueryParameter("statusBarHeight", statusBarHeight.toString()).toString()
            loadUrl(incomeUrl!!)
        } else if (!incomeContent.isNullOrEmpty()) {
            webView.loadDataWithBaseURL(null, incomeContent!!, "text/html", "UTF-8", null)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isLoadUrl && !incomeUrl.isNullOrEmpty() && isNeedLogin(incomeUrl!!)) {
            val uri = Uri.parse(incomeUrl)
            val token = uri.getQueryParameter("token")
            if (token.isNullOrEmpty() && GlobalValue.isLogin()) {
                incomeUrl = appendLoginInfo(incomeUrl!!)
                loadUrl(incomeUrl!!)
            }
        }
    }

    private fun appendLoginInfo(url: String): String {
        return Uri.parse(url).buildUpon()
            .appendQueryParameter("token", GlobalValue.userInfoBean?.token?.token)
            .appendQueryParameter("expire", GlobalValue.userInfoBean?.token?.expire)
            .appendQueryParameter("sign", GlobalValue.userInfoBean?.token?.sign)
            .appendQueryParameter("gid", GlobalValue.userInfoBean?.token?.gid.toString())
            .appendQueryParameter("uid", GlobalValue.userInfoBean?.token?.uid.toString()).build()
            .toString()
    }

    /**
     * 是否需要header， 网页直接沉浸式
     */
    private fun isHideHeader(url: String): Boolean {
        if (url.isEmpty()) {
            return false
        }
        val uri = Uri.parse(url)
        return uri.getQueryParameter("hideHeader") == "1"
    }

    private fun isNeedLogin(url: String): Boolean {
        if (url.isEmpty()) {
            return false
        }
        val uri = Uri.parse(url)
        return uri.getQueryParameter("isNeedLogin") == "1"
    }

    private fun loadUrl(url: String) {
        isLoadUrl = true
        val params = intent.getStringExtra(postParams)
        if (params.isNullOrEmpty()) {
            webView.loadUrl(url)
        } else {
//            val json = JSONObject(params)
//            val stringBuffer = StringBuffer()
//            for (key in json.keys()) {
//                stringBuffer.append(key + "=" + json.optString(key) + "&")
//            }
//            val result = stringBuffer.removeRange(stringBuffer.length - 1, stringBuffer.length)
//                .toString()
//            webView.postUrl(url, result.toByteArray())
            val json = JSONObject(params)
            val urlBuilder = Uri.parse(url).buildUpon()
            for (key in json.keys()) {
                urlBuilder.appendQueryParameter(key, json.optString(key))
            }
            webView.loadUrl(urlBuilder.build().toString())
        }
    }

    override fun onDestroy() {
        webView.pauseTimers()
        webView.clearCache(true)
        webView.clearFormData()
        webView.clearHistory()
        webView.clearMatches()
        webView.destroy()
        clearCookies()
        super.onDestroy()
    }

    private fun clearCookies() {
        CookieManager.getInstance().removeSessionCookies {
            println("removeSessionCookies $it")
        }
        CookieManager.getInstance().removeAllCookies {
            println("clear web cookie $it")
        }
    }

//    override fun onBackPressed() {
//        if (intent.getStringExtra(postParams) != null) {
//            setResult(RESULT_OK)
//            finish()
//        } else if (webView.canGoBack()) {
//            webView.goBack()
//        } else {
//            setResult(RESULT_OK)
//            finish()
//        }
//    }
//
//    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            if (webView.canGoBack()) {
//                webView.goBack()
//                return true
//            }
//        }
//        return super.onKeyDown(keyCode, event)
//    }

    private fun initWebView() {
        webView.settings.pluginState = WebSettings.PluginState.ON
        webView.settings.javaScriptEnabled = true
        webView.settings.displayZoomControls = false
        webView.settings.useWideViewPort = false
        webView.settings.databaseEnabled = true
        webView.settings.domStorageEnabled = true
//        webView.settings.setAppCacheEnabled(true)
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.loadsImagesAutomatically = true

        webView.addJavascriptInterface(JavaScriptObject(), "androidNative")

        webView.webChromeClient = object : WebChromeClient() {

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                if (incomeTitle.isNullOrEmpty()) {
                    title?.let { mTitle?.text = it }
                }
            }

        }
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                webView.loadUrl("javascript:function CloseMe(msg){androidNative.CloseMe(msg)}")
                webView.loadUrl("javascript:function setTranslation(statusColor, headerColor){androidNative.setTranslation(statusColor, headerColor)}")
                webView.loadUrl("javascript:function chat(chatType, modeMessage, nickName, userImage, toUid){androidNative.chat(chatType, modeMessage, nickName, userImage, toUid)}")
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return super.shouldOverrideUrlLoading(view, request)
            }
        }
    }

    inner class JavaScriptObject {
        @JavascriptInterface
        fun CloseMe(msg: String?) {
            when {
                msg.isNullOrEmpty() || msg == "failed" -> {
                    finish()
                }

                msg == "success" -> {
                    paySuccess()
                }

                msg == "resultOk" -> {
                    setResult(RESULT_OK)
                    finish()
                }

                else -> {
                    ToastUtils.showLong(msg)
                }
            }
        }

        /**
         * 设置状态栏和header背景色
         */
        @JavascriptInterface
        fun setTranslation(statusBarColor: String?, headerColor: String?) {
            Handler(Looper.getMainLooper()).post {
                statusBarColor?.let {
                    StatusBarUtil.setColor(this@WebViewActivity, Color.parseColor(statusBarColor), 0)
                }
                headerColor?.let {
                    mHeader?.setBackgroundColor(Color.parseColor(headerColor))
                }
            }
        }

        @JavascriptInterface
        fun chat(chatType: Int, modeMessage: String?, nickName: String?, userImage: String?, toUid: String?) {
            startChat(chatType, modeMessage, nickName, userImage, toUid)
        }
    }

    open fun paySuccess() {
        startActivity(Intent(this, CardPaySuccessActivity::class.java))
        finish()
    }


    /**
     * 客服聊天
     * @param chatType 0-普通聊天  1-普通客服  2-tgc客服   3-充值客服。
     * @param modeMessage 客服聊天自动发送模版信息
     * @param nickName 聊天对象的昵称
     * @param userImage 聊天对象头像
     * @param toUid 聊天对象的uid
     *
     * 当chatType=0，用户昵称、头像、uid必传
     * chatType是任意客服，modeMessage可选，有就会自动发送
     */
    open fun startChat(chatType: Int, modeMessage: String?, nickName: String?, userImage: String?, toUid: String?) {
//        val intent = Intent(this, ChatActivity::class.java)
//        intent.putExtra(ChatActivity.CHAT_TYPE, chatType)
//        intent.putExtra(ChatActivity.SEND_MODEL_MESSAGE, modeMessage)
//        intent.putExtra(ChatActivity.NICK_NAME, nickName)
//        intent.putExtra(ChatActivity.USER_IMAGE, userImage)
//        intent.putExtra(ChatActivity.USER_ID, toUid)
//        startActivity(intent)
        val params = JumpUtils.appendJumpParam(
            "com.cqcsy.lgsp.upper.chat.ChatActivity",
            mutableMapOf(
                "chatType" to chatType,
                "sendModelMessage" to modeMessage,
                "nickName" to nickName,
                "userImage" to userImage,
                "userId" to toUid
            ),
            true
        )
        JumpUtils.jumpAnyUtils(this, params)
    }
}
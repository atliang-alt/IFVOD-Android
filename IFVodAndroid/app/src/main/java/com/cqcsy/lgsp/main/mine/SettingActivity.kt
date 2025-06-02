package com.cqcsy.lgsp.main.mine

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import cn.jpush.android.api.JPushInterface
import com.blankj.utilcode.util.*
import com.cqcsy.lgsp.BuildConfig
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.app.VideoApplication
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.base.WebViewActivity
import com.cqcsy.lgsp.event.CleanCacheEvent
import com.cqcsy.lgsp.login.AccountHelper
import com.cqcsy.lgsp.login.LoginActivity
import com.cqcsy.lgsp.main.CleanTask
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.views.dialog.HostSelectDialog
import com.cqcsy.lgsp.views.dialog.UpdateDialog
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.network.H5Address
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.AppLanguageUtils
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.views.TipsDialog
import com.google.gson.Gson
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.util.*

/**
 * 设置
 */
class SettingActivity : NormalActivity() {
    private val setAccountCode = 1001
    var updateInfo: JSONObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.setting)
        pushMessageState.isChecked =
            SPUtils.getInstance().getBoolean(Constant.KEY_PUSH_MESSAGE_STATUS, true)
        followMessageState.isChecked = GlobalValue.userInfoBean?.isOnlyAcceptFriend ?: false
        pushMessageState.setOnClickListener {
            if (!pushMessageState.isChecked) {
                showClosePush()
            } else {
                JPushInterface.resumePush(application)
                SPUtils.getInstance()
                    .put(Constant.KEY_PUSH_MESSAGE_STATUS, pushMessageState.isChecked)
            }
        }
        versionName.text = getString(R.string.current_version, AppUtils.getAppVersionName())
        if (!GlobalValue.isLogin()) {
            accountItem.visibility = View.GONE
            accountLine.visibility = View.GONE
            followMessageLine.visibility = View.GONE
            followMessageLayout.visibility = View.GONE
            blackListLine.visibility = View.GONE
            blackListText.visibility = View.GONE
            loginOut.visibility = View.GONE
            vote_manager.visibility = View.GONE
        }
        followMessageState.setOnClickListener {
            // 请求接口、只接受互关好友私信
            setFollowMessageState()
        }
        setAreaShow()
        getCacheSize()
        checkVersion()
        changeHttpUrl()
        setSimpleTraditional()
    }

    override fun onResume() {
        super.onResume()
        placeName.text = NormalUtil.getAreaName()
    }

    private fun setSimpleTraditional() {
        when (AppLanguageUtils.getAppliedLanguage()) {
            Locale.CHINESE -> {
                current_language.text = getString(R.string.simple_chinese)
            }

            Locale.TRADITIONAL_CHINESE -> {
                current_language.text = getString(R.string.traditional_chinese)
            }

            else -> {
                current_language.text = getString(R.string.follow_system)
            }
        }
        ClickUtils.applySingleDebouncing(simple_to_traditional) {
            showLanguageSwitchDialog()
        }
    }

    private fun setAreaShow() {
        if (SPUtils.getInstance().getBoolean(Constant.KEY_SHOW_AREA_SETTING, false)) {
            areaLine.visibility = View.VISIBLE
            areaContent.visibility = View.VISIBLE
        } else {
            areaLine.visibility = View.GONE
            areaContent.visibility = View.GONE
        }
        updateTag.setOnLongClickListener {
            if (areaContent.visibility == View.GONE) {
                SPUtils.getInstance().put(Constant.KEY_SHOW_AREA_SETTING, true)
                areaLine.visibility = View.VISIBLE
                areaContent.visibility = View.VISIBLE
            } else {
                SPUtils.getInstance().put(Constant.KEY_SHOW_AREA_SETTING, false)
                areaLine.visibility = View.GONE
                areaContent.visibility = View.GONE
            }
            true
        }
    }

    private fun setFollowMessageState() {
        HttpRequest.post(RequestUrls.SWITCH_FRIEND_MESSAGE, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response != null) {
                    val isStatus = response.optBoolean("isOnlyAcceptFriend")
                    followMessageState.isChecked = isStatus
                    if (GlobalValue.userInfoBean?.isOnlyAcceptFriend != isStatus) {
                        GlobalValue.userInfoBean?.isOnlyAcceptFriend = isStatus
                        val jsonString = Gson().toJson(GlobalValue.userInfoBean)
                        SPUtils.getInstance().put(
                            Constant.KEY_USER_INFO,
                            EncodeUtils.base64Encode2String(jsonString.toByteArray())
                        )
                    }
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, tag = this)
    }

    private fun checkVersion() {
        HttpRequest.get(RequestUrls.CHECK_VERSION, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response != null && response.length() > 0) {
                    updateInfo = response
                    val drawable = getDrawable(R.drawable.red_circle_bg)
                    drawable?.setBounds(0, 0, SizeUtils.dp2px(5f), SizeUtils.dp2px(5f))
                    updateTag.setCompoundDrawables(
                        null,
                        null,
                        drawable,
                        null
                    )
                }
            }

            override fun onError(response: String?, errorMsg: String?) {

            }
        }, tag = this)
    }

    private fun showUpdate(json: JSONObject) {
        val dialog = UpdateDialog(this)
        dialog.setUpdateInfo(json)
        dialog.show()
    }

    fun showUpdate(view: View) {
        if (updateInfo == null) {
            ToastUtils.showShort(R.string.current_is_newer)
        } else {
            showUpdate(updateInfo!!)
        }
    }

    private fun showClosePush() {
        val dialog = TipsDialog(this)
        dialog.setDialogTitle(R.string.message_setting)
        dialog.setMsg(R.string.close_push_tip)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setLeftListener(R.string.close_push) {
            dialog.dismiss()
            JPushInterface.stopPush(application)
            SPUtils.getInstance().put(Constant.KEY_PUSH_MESSAGE_STATUS, pushMessageState.isChecked)
        }
        dialog.setRightListener(R.string.open_push) {
            dialog.dismiss()
            pushMessageState.isChecked = true
        }
        dialog.show()
    }

    override fun getContainerView(): Int {
        return R.layout.activity_setting
    }

    fun loginOut(view: View) {
        showExitLoginDialog()
    }

    private fun showLanguageSwitchDialog() {
        val dialog = LanguageSwitchDialog(this)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }


    private fun showExitLoginDialog() {
        val dialog = TipsDialog(this)
        dialog.setDialogTitle(R.string.tips)
        dialog.setMsg(R.string.exit_login_tip)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setLeftListener(R.string.noExitLogin) {
            dialog.dismiss()
        }
        dialog.setRightListener(R.string.exitLogin) {
            dialog.dismiss()
            exitLogin(false)
        }
        dialog.show()
    }

    private fun exitLogin(isEnterLogin: Boolean) {
        val token = GlobalValue.userInfoBean?.token
        val params = HttpParams()
        params.put("expire", token?.expire)
        params.put("gid", token?.gid.toString())
        params.put("sign", token?.sign)
        params.put("token", token?.token)
        params.put("uid", token?.uid.toString())
        params.put("tag", JPushInterface.getRegistrationID(this))
        HttpRequest.post(RequestUrls.LOGOUT, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                AccountHelper.logout()
                if (isEnterLogin) {
                    startActivity(Intent(this@SettingActivity, LoginActivity::class.java))
                } else {
                    Toast.makeText(this@SettingActivity, "退出登录成功", Toast.LENGTH_SHORT).show()
                }
                finish()
            }

            override fun onError(response: String?, errorMsg: String?) {
            }

        }, params, this)
    }

    fun playAndDownloadSetting(view: View) {
        startActivity(Intent(this, PlayAndDownloadSetting::class.java))
    }

    fun blackListSetting(view: View) {
        startActivity(Intent(this, BlackListManager::class.java))
    }

    fun voteManager(view: View) {
        if (GlobalValue.isLogin()) {
            val intent = Intent(this, MineVoteActivity::class.java)
            startActivity(intent)
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private var mTotalSize = 0L

    private fun getCacheSize() {
        CoroutineScope(Dispatchers.IO).launch {
            mTotalSize = if (CleanTask.isDeleting) {
                0
            } else {
                // 禁止格式化这一行，否则部分手机无法识别
                FileUtils.getLength(GlobalValue.APP_CACHE_PATH)- FileUtils.getLength(GlobalValue.VIDEO_DOWNLOAD_PATH)- FileUtils.getLength(GlobalValue.DOWNLOAD_IMAGE)+FileUtils.getLength(cacheDir) + FileUtils.getLength(externalCacheDir)
            }
            runOnUiThread {
                cacheSize.text = NormalUtil.formatFileSize(this@SettingActivity, mTotalSize)
                clearContent.isEnabled = mTotalSize != 0L
            }
        }
    }

    fun accountSetting(view: View) {
        startActivityForResult(Intent(this, AccountAndSecurityActivity::class.java), setAccountCode)
    }

    fun areaSetting(view: View) {
        startActivity(Intent(this, PlaceSetActivity::class.java))
    }

    fun clearCache(view: View) {
        clearContent.isEnabled = false
        EventBus.getDefault().post(CleanCacheEvent())
        val valueAnimation = ValueAnimator.ofInt(mTotalSize.toInt(), 0)
        valueAnimation.duration = 1500
        valueAnimation.addUpdateListener {
            val value = it.animatedValue.toString().toLong()
            cacheSize.text =
                NormalUtil.formatFileSize(this@SettingActivity, if (value < 0) 0 else value)
            if (value <= 0L && !(application as VideoApplication).isAppBackground) {
                ToastUtils.showShort(R.string.clear_cache_success)
            }
        }
        valueAnimation.start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == setAccountCode) {
                exitLogin(true)
            }
        }
    }

    fun showAbout(view: View) {
        val intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra(WebViewActivity.urlKey, H5Address.ABOUT_US)
        intent.putExtra(WebViewActivity.titleKey, getString(R.string.about_us))
        startActivity(intent)
    }

    fun showAgreement(view: View) {
        val intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra(WebViewActivity.urlKey, H5Address.USER_AGREEMENT)
        intent.putExtra(WebViewActivity.titleKey, getString(R.string.confidentialityAgreement))
        startActivity(intent)
    }

    private fun changeHttpUrl() {
        if (BuildConfig.BUILD_TYPE != "release") {
            loginOut.setOnLongClickListener {
                HostSelectDialog(this).show()
                true
            }
        }
    }
}
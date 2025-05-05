package com.cqcsy.lgsp.vip

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.bean.AreaBean
import com.cqcsy.library.pay.area.AreaSelectActivity
import com.cqcsy.lgsp.login.LoginActivity
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.vip.util.VipPayBackDialog
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.bean.UserInfoBean
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.library.views.TipsDialog
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_help_friends_open_vip.*
import kotlinx.android.synthetic.main.layout_sure_account_dialog.view.*
import org.json.JSONObject

/**
 * 帮助好友开通VIP
 */
class HelpFriendOpenVip : NormalActivity() {
    private val areaResultCode = 1001
    private val selectClassifyCode = 1002
    private val mobilType = 2
    private val emailType = 1

    // 账号类型
    private var type = mobilType

    // 地区码
    private var areaCode = ""
    private var accountName = ""
    private var toUid = 0
    private var selectId = ""
    private var categoryId = ""

    private var isShowBackDialog: Boolean = true

    override fun getContainerView(): Int {
        return R.layout.activity_help_friends_open_vip
    }

    override fun onViewCreate() {
        super.onViewCreate()
        setHeaderTitle(R.string.helpFriendOpenVip)
        setupTab()
        val uid = intent.getStringExtra("targetUid")
        selectId = intent.getStringExtra("selectId") ?: ""
        categoryId = intent.getStringExtra("categoryId") ?: ""
        if (!uid.isNullOrEmpty()) {
            getUserInfo(uid)
        }
    }

    private fun setupTab() {
        typeLayout.addTab(typeLayout.newTab().setText(R.string.friendPhoneNumb), true)
        typeLayout.addTab(typeLayout.newTab().setText(R.string.friendEmail))
        typeLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.customView = null
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                val textView =
                    LayoutInflater.from(this@HelpFriendOpenVip)
                        .inflate(R.layout.layout_help_vip_tab_text, null) as TextView
                textView.text = tab?.text
                tab?.customView = textView
                if (typeLayout.selectedTabPosition == 0) {
                    type = mobilType
                    phoneEditLayout.visibility = View.VISIBLE
                    emailEditLayout.visibility = View.GONE
                } else {
                    type = emailType
                    phoneEditLayout.visibility = View.GONE
                    emailEditLayout.visibility = View.VISIBLE
                }
                NormalUtil.clearTabLayoutTips(typeLayout)
            }
        })
    }

    /**
     * 下一步
     */
    fun nextBtn(view: View) {
        if (!GlobalValue.isLogin()) {
            startActivity(Intent(this, LoginActivity::class.java))
            return
        }
        val params = HttpParams()
        if (type == emailType) {
            accountName = emailEdit.text.toString()
            if (accountName.isEmpty()) {
                ToastUtils.showLong(R.string.email_or_account_tips)
                return
            }
        } else {
            accountName = phoneNumbEdit.text.toString()
            if (areaCode.isEmpty()) {
                ToastUtils.showLong(R.string.areaTips)
                return
            }
            if (accountName.isEmpty()) {
                ToastUtils.showLong(R.string.phoneTips)
                return
            }
            params.put("AreaCode", areaCode)
        }
        params.put("RegType", type)
        params.put("AccountName", accountName)
        params.put("status", 1)
        showProgressDialog()
        checkAccount(params)
    }

    private fun getUserInfo(uid: String) {
        showProgressDialog()
        val params = HttpParams()
        params.put("userId", uid)
        HttpRequest.get(RequestUrls.USER_INFO, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgressDialog()
                if (response != null) {
                    val userInfo = Gson().fromJson<UserInfoBean>(
                        response.toString(),
                        object : TypeToken<UserInfoBean>() {}.type
                    )
                    type = emailType
                    accountName = userInfo.userNameRaw ?: ""
                    toUid = uid.toInt()
                    showSureDialog(
                        userInfo.userNameRaw ?: "",
                        userInfo.nickName ?: "",
                        userInfo.avatar ?: ""
                    )
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgressDialog()
            }
        }, params, this)
    }

    /**
     * 验证账号是否可用
     */
    private fun checkAccount(params: HttpParams) {
        HttpRequest.post(RequestUrls.ACCOUNT_CHECK, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                toUid = response.optInt("uid", 0)
                dismissProgressDialog()
                when {
                    toUid <= 0 -> {
                        ToastUtils.showLong(R.string.disableAccount)
                    }

                    toUid == GlobalValue.userInfoBean?.id -> {
                        showTipDialog()
                    }

                    else -> {
                        val userName = response.optString("userNameRaw")
                        val nickName = response.optString("nickName")
                        val avatar = response.optString("avatar")
                        showSureDialog(userName, nickName, avatar)
                    }
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgressDialog()
                ToastUtils.showLong(errorMsg)
            }

        }, params, this)
    }

    /**
     * 账号是自己的提示
     */
    private fun showTipDialog() {
        val tipsDialog = TipsDialog(this)
        tipsDialog.setDialogTitle(R.string.tips)
        tipsDialog.setMsg(R.string.account_tips)
        tipsDialog.setLeftListener(R.string.renew) {
            tipsDialog.dismiss()
            val intent = Intent(this, OpenVipActivity::class.java)
            intent.putExtra("pathInfo", this.javaClass.simpleName)
            startActivity(intent)
            finish()
        }
        tipsDialog.setRightListener(R.string.known) {
            tipsDialog.dismiss()
        }
        tipsDialog.show()
    }

    /**
     * 确认账号信息
     */
    private fun showSureDialog(userName: String, nickName: String, photo: String) {
        val dialog = Dialog(this, R.style.dialog_style)
        val view = View.inflate(this, R.layout.layout_sure_account_dialog, null)
        view.nickName.text = nickName
        if (type == emailType) {
            view.account.text = userName
        } else {
            view.account.text = "$areaCode $accountName"
        }
        ImageUtil.loadCircleImage(this, photo, view.useLogo)
        view.close.setOnClickListener {
            val toAccount = if (type == emailType) {
                accountName
            } else {
                "$areaCode $accountName"
            }
            val intent = Intent(this, HelpFriendOpenVipSelect::class.java)
            intent.putExtra("toUid", toUid)
            intent.putExtra("toAccount", toAccount)
            intent.putExtra("toUserName", userName)
            intent.putExtra("toNickName", nickName)
            intent.putExtra("selectId", selectId)
            intent.putExtra("categoryId", categoryId)
            startActivityForResult(intent, selectClassifyCode)
            dialog.dismiss()
        }
        dialog.setContentView(view)
        dialog.show()
        val attribute = dialog.window?.attributes
        val width = resources.displayMetrics.widthPixels
        attribute?.height = WindowManager.LayoutParams.WRAP_CONTENT
        attribute?.width = (width * 0.72f).toInt()
        attribute?.gravity = Gravity.CENTER
        dialog.window?.attributes = attribute
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == areaResultCode) {
                val areaBean =
                    data?.getSerializableExtra(AreaSelectActivity.selectedArea) as AreaBean
                areaCode = "+" + areaBean.code_Tel!!
                loginAreaNumb.text = areaCode
            }
            if (requestCode == selectClassifyCode) {
                isShowBackDialog = false
            }
        }
    }

    override fun onBackPressed() {
        if (isShowBackDialog) {
            dialogShow()
        } else {
            finish()
        }
    }

    private fun dialogShow() {
        val vipPayBackDialog = VipPayBackDialog(this, object : VipPayBackDialog.OnClickListener {
            override fun onBuy() {
            }

            override fun onBack() {
                finish()
            }
        })
        vipPayBackDialog.show()
    }

    /**
     * 选择地区
     */
    fun areaNumbClick(view: View) {
        startActivityForResult(Intent(this, AreaSelectActivity::class.java), areaResultCode)
    }

}
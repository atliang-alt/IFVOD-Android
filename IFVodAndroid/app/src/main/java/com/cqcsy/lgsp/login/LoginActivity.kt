package com.cqcsy.lgsp.login

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.blankj.utilcode.util.*
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.BaseActivity
import com.cqcsy.library.base.WebViewActivity
import com.cqcsy.library.bean.AreaBean
import com.cqcsy.library.bean.UserInfoBean
import com.cqcsy.library.event.LoginEvent
import com.cqcsy.lgsp.main.SocketClient
import com.cqcsy.library.network.H5Address
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.lgsp.push.PushPresenter
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.StatusBarUtil
import com.cqcsy.library.views.BottomBaseDialog
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.lgsp.views.dialog.NoticeDialog
import com.cqcsy.library.pay.area.AreaSelectActivity
import com.cqcsy.library.utils.GlobalValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.emailEdit
import kotlinx.android.synthetic.main.layout_forget_password_popup.view.*
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.io.Serializable

/**
 * 登录页
 */
class LoginActivity : BaseActivity() {
    private val areaResultCode = 1001
    private val registerCode = 1002
    private val forgetPasswordCode = 1003

    // 地区码
    private var areaCode = ""
    private var accountName = ""
    private var areaList: MutableList<AreaBean>? = null

    // 注册类型
    private var type = Constant.MOBIL_TYPE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        StatusBarUtil.setTranslucentForImageView(this, 0, null)
        initView()
    }

    private fun initView() {
        if (SPUtils.getInstance().getBoolean(Constant.IS_NOTCH_IN_SCREEN)) {
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                SizeUtils.dp2px(176f)
            )
            loginLogo.layoutParams = layoutParams
            val params = RelativeLayout.LayoutParams(
                SizeUtils.dp2px(48f),
                SizeUtils.dp2px(44f)
            )
            params.topMargin = SizeUtils.dp2px(20f)
            loginBack.layoutParams = params
            val logoParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            logoParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
            logoParams.topMargin = SizeUtils.dp2px(30f)
            logoImage.layoutParams = logoParams
        }
        setAreaList()
    }

    private fun setAreaList() {
        val list = NormalUtil.getLocalAreaList()
        if (list.isNullOrEmpty()) {
            getAreaList()
        } else {
            getLocation(list)
        }
    }

    /**
     * 获取定位国家
     */
    private fun getLocation(list: MutableList<AreaBean>) {
        HttpRequest.get(RequestUrls.GET_USER_REGION, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val currentCountry = response?.optString("code")
                list.forEach {
                    if (it.code == currentCountry) {
                        areaCode = "+" + it.code_Tel
                        loginAreaNumb.text = areaCode
                    }
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(R.string.location_permission_deny)
            }
        }, tag = this)
    }

    /**
     * 获取国家区域码
     */
    private fun getAreaList() {
        HttpRequest.get(RequestUrls.COUNTRY_LIST, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val jsonArray = response?.optJSONArray("country")
                if (jsonArray == null || jsonArray.length() == 0) {
                    return
                }
                areaList = Gson().fromJson<ArrayList<AreaBean>>(
                    jsonArray.toString(),
                    object : TypeToken<ArrayList<AreaBean>>() {}.type
                )
                response.put(Constant.KEY_COUNTRY_AREA_INFO_TIME, System.currentTimeMillis())
                SPUtils.getInstance()
                    .put(Constant.KEY_COUNTRY_AREA_INFO, response.toString())
                val currentCountry = response.optString("current")
                areaList?.forEach {
                    if (it.code == currentCountry) {
                        areaCode = "+" + it.code_Tel
                        loginAreaNumb.text = areaCode
                    }
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
            }
        }, tag = this)
    }

    fun areaClick(view: View) {
        val intent = Intent(this, AreaSelectActivity::class.java)
        if (!areaList.isNullOrEmpty()) {
            intent.putExtra(AreaSelectActivity.areas, areaList as Serializable)
        }
        startActivityForResult(intent, areaResultCode)
    }

    fun login(view: View) {
        val params = HttpParams()
        params.put("RegType", type)
        if (type == Constant.EMAIL_TYPE) {
            accountName = emailEdit.text.toString().trim()
            if (emailEdit.text.isEmpty()) {
                errorTips.text = resources.getString(R.string.emailTips)
                return
            }
        } else {
            params.put("AreaCode", areaCode)
            accountName = phoneNumbEdit.text.toString().trim()
            if (areaCode.isEmpty()) {
                errorTips.text = resources.getString(R.string.areaTips)
                return
            }
            if (phoneNumbEdit.text.isEmpty()) {
                errorTips.text = resources.getString(R.string.phoneTips)
                return
            }
        }
        if (loginEditPassword.text.isEmpty()) {
            errorTips.text = resources.getString(R.string.passwordTips)
            return
        }
        showProgressDialog()
        params.put("AccountName", accountName)
        params.put("UserPwd", loginEditPassword.text.toString())
        if (!DeviceUtils.isEmulator() && NormalUtil.isPhone(this)) {
            params.put("deviceID", EncryptUtils.encryptMD5ToString(DeviceUtils.getMacAddress()))
        }
        HttpRequest.post(RequestUrls.LOGIN, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgressDialog()
                saveData(response)
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgressDialog()
                errorTips.text = errorMsg
            }
        }, params, this)
    }

    private fun saveData(response: JSONObject?) {
        val bean = Gson().fromJson(response.toString(), UserInfoBean::class.java)
        SPUtils.getInstance().put(
            Constant.KEY_USER_INFO,
            EncodeUtils.base64Encode2String(response.toString().toByteArray())
        )
        GlobalValue.userInfoBean = bean
        PushPresenter.bindTag(applicationContext)
        SocketClient.userLogin()
        ToastUtils.showLong(R.string.login_success)
        if (bean.isGiveVip && bean.isEnable) {
            val noticeDialog = NoticeDialog(this@LoginActivity)
            noticeDialog.setData("", bean.alterDesc ?: "")
            noticeDialog.setOnDismissListener {
                loginSuccess()
            }
            noticeDialog.show()
        } else {
            loginSuccess()
        }
    }

    private fun loginSuccess() {
        val event = LoginEvent()
        event.status = true
        EventBus.getDefault().post(event)
        setResult(Activity.RESULT_OK)
        finish()
    }

    fun phoneLoginLayout(view: View) {
        type = Constant.MOBIL_TYPE
        phoneLoginText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        phoneLoginText.setTextColor(ColorUtils.getColor(R.color.word_color_2))
        phoneLoginText.setTypeface(Typeface.DEFAULT, Typeface.BOLD)

        emailLoginText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        emailLoginText.setTextColor(ColorUtils.getColor(R.color.grey))
        phoneLoginText.setTypeface(Typeface.DEFAULT, Typeface.NORMAL)

        phoneLoginLine.visibility = View.VISIBLE
        emailLoginLine.visibility = View.GONE
        phoneNumbEditLayout.visibility = View.VISIBLE
        emailEdit.visibility = View.GONE

        editTipsText.text = resources.getString(R.string.phoneNumber)
        errorTips.text = ""
        loginEditPassword.setText("")
    }

    fun emailLoginLayout(view: View) {
        type = Constant.EMAIL_TYPE
        phoneLoginText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        phoneLoginText.setTextColor(ColorUtils.getColor(R.color.grey))
        phoneLoginText.setTypeface(Typeface.DEFAULT, Typeface.NORMAL)

        emailLoginText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        emailLoginText.setTextColor(ColorUtils.getColor(R.color.word_color_2))
        phoneLoginText.setTypeface(Typeface.DEFAULT, Typeface.BOLD)

        phoneLoginLine.visibility = View.GONE
        emailLoginLine.visibility = View.VISIBLE
        phoneNumbEditLayout.visibility = View.GONE
        emailEdit.visibility = View.VISIBLE

        editTipsText.text = resources.getString(R.string.accountNumber)
        errorTips.text = ""
        loginEditPassword.setText("")
    }

    fun forgetPassword(view: View) {
        val dialog = object : BottomBaseDialog(this) {}
        val contentView = View.inflate(this, R.layout.layout_forget_password_popup, null)
        contentView.phoneRetrieveLayout.setOnClickListener {
            // 跳转到手机找回密码
            val intent = Intent(this, RetrievePassword::class.java)
            intent.putExtra("formId", 0)
            startActivityForResult(intent, forgetPasswordCode)
            dialog.dismiss()
        }
        contentView.emailRetrieveLayout.setOnClickListener {
            // 跳转到邮箱找回密码
            val intent = Intent(this, RetrievePassword::class.java)
            intent.putExtra("formId", 1)
            startActivityForResult(intent, forgetPasswordCode)
            dialog.dismiss()
        }
        contentView.cancelRetrievePassword.setOnClickListener {
            // 取消
            dialog.dismiss()
        }
        dialog.setContentView(contentView)
        dialog.show()
    }

    fun lookPassword(view: View) {
        if (lookPasswordImage.isSelected) {
            // 密码可见
            loginEditPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            lookPasswordImage.isSelected = false
        } else {
            // 不可见
            loginEditPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            lookPasswordImage.isSelected = true
        }
        loginEditPassword.setSelection(loginEditPassword.text.length)
    }

    fun startRegister(view: View) {
        startActivityForResult(Intent(this, RegisterActivity::class.java), registerCode)
    }

    fun backLogin(view: View) {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissProgressDialog()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                areaResultCode -> {
                    val areaBean =
                        data?.getSerializableExtra(AreaSelectActivity.selectedArea) as AreaBean
                    areaCode = "+" + areaBean.code_Tel!!
                    loginAreaNumb.text = areaCode
                }
                registerCode -> {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun showAgreement(view: View) {
        val intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra(WebViewActivity.urlKey, H5Address.USER_AGREEMENT)
        intent.putExtra(WebViewActivity.titleKey, getString(R.string.confidentialityAgreement))
        startActivity(intent)
    }
}
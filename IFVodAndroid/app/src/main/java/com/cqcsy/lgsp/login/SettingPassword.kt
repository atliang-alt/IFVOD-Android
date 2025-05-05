package com.cqcsy.lgsp.login

import android.app.Activity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.core.widget.addTextChangedListener
import com.blankj.utilcode.util.*
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.bean.UserInfoBean
import com.cqcsy.library.event.LoginEvent
import com.cqcsy.lgsp.main.SocketClient
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.lgsp.push.PushPresenter
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.views.dialog.NoticeDialog
import com.cqcsy.library.utils.GlobalValue
import com.google.gson.Gson
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_setting_password.*
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

/**
 * 设置密码及昵称
 */
class SettingPassword : NormalActivity() {
    private var type = Constant.MOBIL_TYPE
    private var accountName = ""
    private var areaCode = ""
    private var validateCode = ""
    private var invitation = ""
    override fun getContainerView(): Int {
        return R.layout.activity_setting_password
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(resources.getString(R.string.settingPasswordAndName))
        initData()
        initView()
    }

    private fun initData() {
        type = intent.getIntExtra("RegType", Constant.MOBIL_TYPE)
        accountName = intent.getStringExtra("AccountName") ?: ""
        areaCode = intent.getStringExtra("AreaCode") ?: ""
        validateCode = intent.getStringExtra("ValidateCode") ?: ""
        invitation = intent.getStringExtra("InviteCode") ?: ""
    }

    private fun initView() {
        editOne.addTextChangedListener { text ->
            setRegisterEnable()
            if (text!!.isNotEmpty()) {
                lookPasswordOne.visibility = View.VISIBLE
            } else {
                lookPasswordOne.visibility = View.INVISIBLE
            }
        }
        editTwo.addTextChangedListener { text ->
            setRegisterEnable()
            if (text!!.isNotEmpty()) {
                lookPasswordTwo.visibility = View.VISIBLE
            } else {
                lookPasswordTwo.visibility = View.INVISIBLE
            }
        }
        editName.addTextChangedListener { text ->
            setRegisterEnable()
        }
    }

    private fun setRegisterEnable() {
        finishRegister.isEnabled =
            !editOne.text.isNullOrEmpty() && !editTwo.text.isNullOrEmpty() && !editName.text.isNullOrEmpty() && (select_man.isSelected || select_women.isSelected)
    }

    fun selectSex(view: View) {
        if (view.id == R.id.select_man) {
            select_man.isSelected = true
            select_women.isSelected = false
        } else {
            select_man.isSelected = false
            select_women.isSelected = true
        }
        setRegisterEnable()
    }

    /**
     * 注册
     */
    fun finishRegister(view: View) {
        if (!finishRegister.isEnabled) {
            return
        }
        if (editOne.text.isEmpty()) {
            errorTips.text = resources.getString(R.string.passwordTips)
            return
        }
        if (editTwo.text.isEmpty()) {
            errorTips.text = resources.getString(R.string.passwordTips)
            return
        }
        if (editOne.text.length < 6) {
            errorTips.text = resources.getString(R.string.passwordEmptyTips)
            return
        }
        if (editOne.text.toString() != editTwo.text.toString()) {
            errorTips.text = resources.getString(R.string.passwordNotSame)
            return
        }
        if (editName.text.isEmpty()) {
            errorTips.text = resources.getString(R.string.nick_tips)
            return
        }
        if (!select_man.isSelected && !select_women.isSelected) {
            errorTips.setText(R.string.select_sex)
            return
        }
        showProgressDialog()
        val params = HttpParams()
        params.put("RegType", type)
        params.put("NickName", editName.text.toString().trim())
        params.put("AreaCode", areaCode)
        params.put("ValidateCode", validateCode)
        params.put("AccountName", accountName)
        params.put("Sex", if (select_man.isSelected) 1 else 0)
        params.put("UserPwd", editTwo.text.toString())
        params.put("InviteCode", invitation)
        if (!DeviceUtils.isEmulator() && NormalUtil.isPhone(this)) {
            params.put("deviceID", EncryptUtils.encryptMD5ToString(DeviceUtils.getMacAddress()))
        }
        HttpRequest.post(RequestUrls.REGISTER, object : HttpCallBack<JSONObject>() {
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
        val userInfoBean = Gson().fromJson(response.toString(), UserInfoBean::class.java)
        SPUtils.getInstance().put(
            Constant.KEY_USER_INFO,
            EncodeUtils.base64Encode2String(response.toString().toByteArray())
        )
        GlobalValue.userInfoBean = userInfoBean
        PushPresenter.bindTag(applicationContext)
        SocketClient.userLogin()
        if (userInfoBean.isEnable && userInfoBean.isGiveVip) {
            val noticeDialog = NoticeDialog(this@SettingPassword)
            noticeDialog.setData("", userInfoBean.alterDesc ?: "")
            noticeDialog.setOnDismissListener {
                registerSuccess()
            }
            noticeDialog.show()
        } else {
            registerSuccess()
        }
    }

    private fun registerSuccess() {
        val event = LoginEvent()
        event.status = true
        EventBus.getDefault().post(event)
        setResult(Activity.RESULT_OK)
        finish()
    }

    /**
     * 第一个输入框密码是否可见
     */
    fun lookPasswordOne(view: View) {
        if (lookPasswordOne.isSelected) {
            // 密码可见
            editOne.transformationMethod = PasswordTransformationMethod.getInstance()
            lookPasswordOne.isSelected = false
        } else {
            // 不可见
            editOne.transformationMethod = HideReturnsTransformationMethod.getInstance()
            lookPasswordOne.isSelected = true
        }
        editOne.setSelection(editOne.text.length)
    }

    /**
     * 第二个输入框密码是否可见
     */
    fun lookPasswordTwo(view: View) {
        if (lookPasswordTwo.isSelected) {
            // 密码可见
            editTwo.transformationMethod = PasswordTransformationMethod.getInstance()
            lookPasswordTwo.isSelected = false
        } else {
            // 不可见
            editTwo.transformationMethod = HideReturnsTransformationMethod.getInstance()
            lookPasswordTwo.isSelected = true
        }
        editTwo.setSelection(editTwo.text.length)
    }
}
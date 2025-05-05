package com.cqcsy.lgsp.main.mine

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.core.widget.addTextChangedListener
import com.blankj.utilcode.util.*
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.bean.AreaBean
import com.cqcsy.library.pay.area.AreaSelectActivity
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.Constant
import com.cqcsy.lgsp.utils.CountDownTimerUtils
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.utils.GlobalValue
import com.google.gson.Gson
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_update_account.*
import org.json.JSONObject

/**
 * 设置 -- 修改账号
 */
class UpdateAccountActivity : NormalActivity() {
    private var type = 0
    private var countDownTimerUtils: CountDownTimerUtils? = null
    private var areaCode = ""
    private var accountName = ""
    // 是否获取验证码
    private var isGetCode = false
    private val areaResultCode = 1001

    override fun getContainerView(): Int {
        return R.layout.activity_update_account
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
        initView()
    }

    private fun initData() {
        type = intent.getIntExtra("RegType", Constant.MOBIL_TYPE)
        if (type == Constant.EMAIL_TYPE) {
            setHeaderTitle(
                StringUtils.getString(
                    R.string.changeAccount, StringUtils.getString(R.string.email)
                )
            )
            editTipsText.text = StringUtils.getString(R.string.email)
            phoneNumbEditLayout.visibility = View.GONE
            emailEdit.visibility = View.VISIBLE
        } else {
            setHeaderTitle(
                StringUtils.getString(
                    R.string.changeAccount, StringUtils.getString(R.string.phoneNumber)
                )
            )
            editTipsText.text = StringUtils.getString(R.string.phoneNumber)
            phoneNumbEditLayout.visibility = View.VISIBLE
            emailEdit.visibility = View.GONE
        }
    }

    private fun initView() {
        phoneNumbEdit.addTextChangedListener { text ->
            sureBtn.isEnabled = !TextUtils.isEmpty(validateCode.text) && text!!.isNotEmpty()
        }
        emailEdit.addTextChangedListener { text ->
            sureBtn.isEnabled = !TextUtils.isEmpty(validateCode.text) && text!!.isNotEmpty()
        }
        validateCode.addTextChangedListener { text ->
            if (type == Constant.MOBIL_TYPE && !TextUtils.isEmpty(phoneNumbEdit.text) && text!!.isNotEmpty()) {
                sureBtn.isEnabled = true
            } else {
                sureBtn.isEnabled = !TextUtils.isEmpty(emailEdit.text) && text!!.isNotEmpty()
            }
        }
    }

    fun selectArea(view: View) {
        startActivityForResult(Intent(this, AreaSelectActivity::class.java), areaResultCode)
    }

    /**
     * 获取验证码
     */
    fun getVerificationCode(view: View) {
        countDownTimerUtils = CountDownTimerUtils(this, getVerificationCode, 60000, 1000)
        val params = HttpParams()
        params.put("RegType", type)
        if (type == Constant.EMAIL_TYPE) {
            accountName = emailEdit.text.toString()
            if (emailEdit.text.isEmpty()) {
                errorTips.text = resources.getString(R.string.emailTips)
                return
            }
            if (!emailEdit.text.toString().contains("@")) {
                errorTips.text = resources.getString(R.string.emailTextTips)
                return
            }
        } else {
            params.put("AreaCode", areaCode)
            accountName = phoneNumbEdit.text.toString()
            if (areaCode.isEmpty()) {
                errorTips.text = resources.getString(R.string.areaTips)
                return
            }
            if (phoneNumbEdit.text.isEmpty()) {
                errorTips.text = resources.getString(R.string.phoneTips)
                return
            }
        }
        params.put("AccountName", accountName)
        HttpRequest.post(RequestUrls.GET_VERIFICATION_CODE, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                isGetCode = true
                countDownTimerUtils?.start()
            }

            override fun onError(response: String?, errorMsg: String?) {
                isGetCode = false
                errorTips.text = errorMsg
            }
        }, params, this)
    }

    /**
     * 确定修改账号
     */
    fun sureNext(view: View) {
        if (!sureBtn.isEnabled) {
            return
        }
        if (!isGetCode) {
            errorTips.text = StringUtils.getString(R.string.getCodeTips)
            return
        }
        if (validateCode.text.isEmpty()) {
            errorTips.text = StringUtils.getString(R.string.verificationCodeTips)
            return
        }
        val params = HttpParams()
        params.put("RegType", type)
        accountName = if (type == Constant.EMAIL_TYPE) {
            emailEdit.text.toString()
        } else {
            params.put("AreaCode", areaCode)
            phoneNumbEdit.text.toString()
        }
        showProgressDialog()
        params.put("ValidateCode", validateCode.text.toString())
        params.put("AccountName", accountName)
        params.put("VerifyAccount", true)
        HttpRequest.post(RequestUrls.CHANGE_ACCOUNT, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                countDownTimerUtils?.cancel()
                countDownTimerUtils?.onFinish()
                dismissProgressDialog()
                saveLocalData()
                setResult(Activity.RESULT_OK)
                finish()
            }

            override fun onError(response: String?, errorMsg: String?) {
                countDownTimerUtils?.cancel()
                countDownTimerUtils?.onFinish()
                dismissProgressDialog()
                errorTips.text = errorMsg
            }
        }, params, this)
    }

    private fun saveLocalData() {
        if (type == Constant.EMAIL_TYPE) {
            GlobalValue.userInfoBean?.email = accountName
        } else {
            GlobalValue.userInfoBean?.areacode = areaCode
            GlobalValue.userInfoBean?.phone = accountName
        }
        val jsonString = Gson().toJson(GlobalValue.userInfoBean)
        SPUtils.getInstance().put(
            Constant.KEY_USER_INFO,
            EncodeUtils.base64Encode2String(jsonString.toByteArray())
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == areaResultCode) {
                val areaBean =
                    data?.getSerializableExtra(AreaSelectActivity.selectedArea) as AreaBean
                areaCode = "+" + areaBean.code_Tel
                loginAreaNumb.text = areaCode
            }
        }
    }
}
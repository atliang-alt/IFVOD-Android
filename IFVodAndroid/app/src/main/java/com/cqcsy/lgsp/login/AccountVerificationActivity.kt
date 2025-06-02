package com.cqcsy.lgsp.login

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.widget.addTextChangedListener
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.lgsp.main.mine.UpdateAccountActivity
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.Constant
import com.cqcsy.lgsp.utils.CountDownTimerUtils
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.library.network.HttpRequest
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_account_verification.*
import kotlinx.android.synthetic.main.layout_register_code_tips.view.*
import org.json.JSONObject

/**
 * 账号验证 -- 找回密码、重设密码、手机号设置、邮箱设置
 */
class AccountVerificationActivity : NormalActivity() {
    private var countDownTimerUtils: CountDownTimerUtils? = null

    // 0:找回密码进入 1:重设密码进入 2:账号设置
    private var formId = 0

    // 注册类型
    private var type = Constant.MOBIL_TYPE

    // 是否获取验证码
    private var isGetCode = false
    private var areaCode = ""
    private var accountName = ""
    private var resetPassword = 1001
    private var updateAccount = 1002

    override fun getContainerView(): Int {
        return R.layout.activity_account_verification
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
        initView()
    }

    override fun onDestroy() {
        cancelCountDown()
        super.onDestroy()
    }

    private fun initData() {
        formId = intent.getIntExtra("formId", 0)
        if (formId == 1) {
            setHeaderTitle(R.string.rebuildPassword)
        } else {
            setHeaderTitle(R.string.retrievePassword)
        }
        type = intent.getIntExtra("type", Constant.MOBIL_TYPE)
        if (intent.getStringExtra("areaCode") != null) {
            areaCode = intent.getStringExtra("areaCode") ?: ""
        }
        accountName = intent.getStringExtra("accountName") ?: ""
    }

    private fun initView() {
        if (type == Constant.EMAIL_TYPE) {
            numberText.text = NormalUtil.formatEmail(accountName)
            checkTips.text = resources.getString(R.string.checkEmailNumber)
            verificationCodeImage.setImageResource(R.mipmap.icon_email)
        } else {
            numberText.text = NormalUtil.formatPhoneNumber(accountName)
        }
        verificationCodeEdit.addTextChangedListener { text ->
            confirmBtn.isEnabled = text!!.isNotEmpty()
        }
    }

    fun confirmBtn(view: View) {
        if (!confirmBtn.isEnabled) {
            return
        }
        if (verificationCodeEdit.text.isEmpty()) {
            errorTips.text = resources.getString(R.string.verificationCodeTips)
            return
        }
        if (!isGetCode) {
            errorTips.text = resources.getString(R.string.getCodeTips)
            return
        }
        if (formId == 2) {
            cancelCountDown()
            updateAccount()
        } else {
            val intent = Intent(this@AccountVerificationActivity, ResetPassword::class.java)
            intent.putExtra("AccountName", accountName)
            intent.putExtra("AreaCode", areaCode)
            intent.putExtra("RegType", type)
            intent.putExtra("formId", formId)
            intent.putExtra("ValidateCode", verificationCodeEdit.text.toString().trim())
            startActivityForResult(intent, resetPassword)
        }
    }

    private fun cancelCountDown() {
        countDownTimerUtils?.cancel()
        countDownTimerUtils?.onFinish()
    }

    private fun updateAccount() {
        showProgressDialog()
        val params = HttpParams()
        params.put("RegType", type)
        if (type == Constant.MOBIL_TYPE) {
            params.put("AreaCode", areaCode)
        }
        params.put("ValidateCode", verificationCodeEdit.text.toString().trim())
        params.put("AccountName", accountName)
        params.put("VerifyAccount", true)
        HttpRequest.post(RequestUrls.JUDGE_VALID_CODES, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                cancelCountDown()
                dismissProgressDialog()
                val intent = Intent(this@AccountVerificationActivity, UpdateAccountActivity::class.java)
                intent.putExtra("RegType", type)
                startActivityForResult(intent, updateAccount)
            }

            override fun onError(response: String?, errorMsg: String?) {
                cancelCountDown()
                dismissProgressDialog()
                errorTips.text = errorMsg
            }
        }, params, this)
    }

    /**
     * 获取验证码
     */
    fun getVerificationCode(view: View) {
        countDownTimerUtils = CountDownTimerUtils(this, getVerificationCode, 60000, 1000)
        val params = HttpParams()
        params.put("RegType", type)
        if (type == Constant.MOBIL_TYPE) {
            params.put("AreaCode", areaCode)
        }
        params.put("AccountName", accountName)
        params.put("VerifyAccount", 1)
        HttpRequest.post(RequestUrls.GET_VERIFICATION_CODE, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                isGetCode = true
                if (type == Constant.MOBIL_TYPE) {
                    countDownTimerUtils?.setListener(popupListener)
                }
                countDownTimerUtils?.start()
            }

            override fun onError(response: String?, errorMsg: String?) {
                isGetCode = false
                errorTips.text = errorMsg
            }
        }, params, this)
    }

    /**
     * 监听倒计时10s弹出提示框
     */
    private val popupListener: CountDownTimerUtils.CallBackListener =
        object : CountDownTimerUtils.CallBackListener {
            override fun callBack() {
                if (formId == 0 && type == Constant.MOBIL_TYPE) {
                    showPopup()
                }
            }
        }

    private fun showPopup() {
        val view = LayoutInflater.from(this@AccountVerificationActivity)
            .inflate(R.layout.layout_register_code_tips, null)
        val popupWindow = PopupWindow(
            view,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        /*view.goToEmail.text = resources.getString(R.string.emailGetPassword)
        view.goToEmail.setOnClickListener {
            popupWindow.dismiss()
        }*/
        view.close.setOnClickListener {
            // 取消
            popupWindow.dismiss()
        }
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.contentView = view
        popupWindow.isOutsideTouchable = true
        if (!isFinishing) {
            popupWindow.showAsDropDown(verificationCodeEdit, 0, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            setResult(RESULT_OK)
            finish()
        } else if (resultCode == RESULT_CANCELED) {

        }
    }
}
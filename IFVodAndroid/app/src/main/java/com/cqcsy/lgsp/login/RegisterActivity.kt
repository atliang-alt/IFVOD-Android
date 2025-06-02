package com.cqcsy.lgsp.login

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.widget.addTextChangedListener
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.base.WebViewActivity
import com.cqcsy.library.bean.AreaBean
import com.cqcsy.library.network.H5Address
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.Constant
import com.cqcsy.lgsp.utils.CountDownTimerUtils
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.pay.area.AreaSelectActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.editTipsText
import kotlinx.android.synthetic.main.activity_register.emailEdit
import kotlinx.android.synthetic.main.activity_register.errorTips
import kotlinx.android.synthetic.main.activity_register.loginAreaNumb
import kotlinx.android.synthetic.main.activity_register.phoneNumbEdit
import kotlinx.android.synthetic.main.activity_register.phoneNumbEditLayout
import kotlinx.android.synthetic.main.layout_register_code_tips.view.*
import org.json.JSONObject
import java.io.Serializable

/**
 * 注册页
 */
class RegisterActivity : NormalActivity() {
    private val areaResultCode = 1001
    private val setPasswordCode = 1002

    // 是否是选中的手机号注册
    private var isPhoneRegister = true

    // 注册类型
    private var type = Constant.MOBIL_TYPE

    // 地区码
    private var areaCode = ""
    private var accountName = ""

    // 是否获取验证码
    private var isGetCode = false
    private var areaList: MutableList<AreaBean>? = null

    private var countDownTimerUtils: CountDownTimerUtils? = null

    override fun getContainerView(): Int {
        return R.layout.activity_register
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(resources.getString(R.string.registerAccount))
        setAreaList()
        initView()
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

    private fun initView() {
        checkInviteShow()
        phoneNumbEdit.addTextChangedListener { text ->
            nextBtn.isEnabled = !TextUtils.isEmpty(validateCode.text) && text!!.isNotEmpty()
        }
        emailEdit.addTextChangedListener { text ->
            nextBtn.isSelected = !TextUtils.isEmpty(validateCode.text) && text!!.isNotEmpty()
        }
        validateCode.addTextChangedListener { text ->
            nextBtn.isEnabled =
                (isPhoneRegister && !TextUtils.isEmpty(phoneNumbEdit.text)) || (!isPhoneRegister && !TextUtils.isEmpty(
                    emailEdit.text
                )) && text!!.isNotEmpty()
        }
    }

    /**
     * 判断邀请码是否可用，手机号注册才能用
     */
    private fun checkInviteShow() {
        if (SPUtils.getInstance().getBoolean(Constant.ACTIVITY_SWITCH) && type == Constant.MOBIL_TYPE) {
            invitationLayout.visibility = View.VISIBLE
        } else {
            invitationLayout.visibility = View.GONE
        }
    }

    fun phoneRegister(view: View) {
        type = Constant.MOBIL_TYPE
        nextBtn.isSelected = false
        emailEdit.setText("")
        validateCode.setText("")
        countDownTimerUtils?.cancel()
        getVerificationCode.text = getString(R.string.getVerificationCode)
        getVerificationCode.setTextColor(ColorUtils.getColor(R.color.blue))
        getVerificationCode.isClickable = true
        isPhoneRegister = true
        phoneNumbEditLayout.visibility = View.VISIBLE
        phoneRegisterLine.visibility = View.VISIBLE
        phoneRegisterText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        phoneRegisterText.setTextColor(ColorUtils.getColor(R.color.word_color_2))

        editTipsText.text = resources.getString(R.string.phoneNumber)
        emailEdit.visibility = View.GONE
        emailRegisterLine.visibility = View.GONE
        emailRegisterText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        emailRegisterText.setTextColor(ColorUtils.getColor(R.color.grey))
        checkInviteShow()
    }

    fun emailRegister(view: View) {
        emailRegister()
    }

    private fun emailRegister() {
        type = Constant.EMAIL_TYPE
        nextBtn.isSelected = false
        phoneNumbEdit.setText("")
        validateCode.setText("")
        countDownTimerUtils?.cancel()
        getVerificationCode.text = getString(R.string.getVerificationCode)
        getVerificationCode.setTextColor(ColorUtils.getColor(R.color.blue))
        getVerificationCode.isClickable = true
        isPhoneRegister = false
        phoneNumbEditLayout.visibility = View.GONE
        phoneRegisterLine.visibility = View.GONE
        phoneRegisterText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        phoneRegisterText.setTextColor(ColorUtils.getColor(R.color.grey))

        editTipsText.text = resources.getString(R.string.email)
        emailEdit.visibility = View.VISIBLE
        emailRegisterLine.visibility = View.VISIBLE
        emailRegisterText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        emailRegisterText.setTextColor(ColorUtils.getColor(R.color.word_color_2))
        checkInviteShow()
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
        errorTips.text = ""
        params.put("AccountName", accountName)
        params.put("IsReg", 1)
        HttpRequest.post(RequestUrls.GET_VERIFICATION_CODE, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                isGetCode = true
                countDownTimerUtils?.setListener(popupListener)
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
                if (type == Constant.EMAIL_TYPE) {
                    return
                }
                if (isSafe() && !isPaused) {
                    showPopup()
                }
            }
        }

    private fun showPopup() {
        val view = LayoutInflater.from(this@RegisterActivity)
            .inflate(R.layout.layout_register_code_tips, null)
        val popupWindow = PopupWindow(
            view,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        view.close.setOnClickListener {
            // 取消
            popupWindow.dismiss()
        }
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.contentView = view
        popupWindow.isOutsideTouchable = false
        popupWindow.showAsDropDown(validateCode, 0, 0)
    }

    /**
     * 下一步
     */
    fun registerNext(view: View) {
        if (!nextBtn.isEnabled) {
            return
        }
        if (validateCode.text.isEmpty()) {
            errorTips.text = resources.getString(R.string.verificationCodeTips)
            return
        }
        if (!isGetCode) {
            errorTips.text = resources.getString(R.string.getCodeTips)
            return
        }
        val params = HttpParams()
        params.put("RegType", type)
        if (!isPhoneRegister) {
            areaCode = ""
        }
        params.put("AreaCode", areaCode)
        params.put("ValidateCode", validateCode.text.toString().trim())
        params.put("AccountName", accountName)
        params.put("InviteCode", invitationCode.text.toString().trim())
        HttpRequest.post(RequestUrls.JUDGE_VALID_CODES, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                countDownTimerUtils?.cancel()
                countDownTimerUtils?.onFinish()
                val intent = Intent(this@RegisterActivity, SettingPassword::class.java)
                intent.putExtra("RegType", type)
                intent.putExtra("AreaCode", areaCode)
                intent.putExtra("AccountName", accountName)
                intent.putExtra("InviteCode", invitationCode.text.toString().trim())
                intent.putExtra("ValidateCode", validateCode.text.toString().trim())
                startActivityForResult(intent, setPasswordCode)
                dismissProgressDialog()
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgressDialog()
                errorTips.text = errorMsg
            }
        }, params, this)
    }

    fun startLogin(view: View) {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    /**
     * 选择国家码
     */
    fun selectArea(view: View) {
        val intent = Intent(this, AreaSelectActivity::class.java)
        if (!areaList.isNullOrEmpty()) {
            intent.putExtra(AreaSelectActivity.areas, areaList as Serializable)
        }
        startActivityForResult(intent, areaResultCode)
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
            if (requestCode == setPasswordCode) {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }


    fun showAgreement(view: View) {
        val intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra(WebViewActivity.urlKey, H5Address.USER_AGREEMENT)
        intent.putExtra(WebViewActivity.titleKey, getString(R.string.confidentialityAgreement))
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimerUtils?.cancel()
        countDownTimerUtils?.onFinish()
        countDownTimerUtils = null
    }
}
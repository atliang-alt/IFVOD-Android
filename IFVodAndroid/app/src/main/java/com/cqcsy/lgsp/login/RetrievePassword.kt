package com.cqcsy.lgsp.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.bean.AreaBean
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.pay.area.AreaSelectActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_retrieve_password.*
import kotlinx.android.synthetic.main.activity_retrieve_password.emailEdit
import kotlinx.android.synthetic.main.activity_retrieve_password.errorTips
import kotlinx.android.synthetic.main.activity_retrieve_password.phoneNumbEdit
import org.json.JSONObject
import java.io.Serializable

/**
 * 找回密码
 */
class RetrievePassword : NormalActivity() {
    private val areaResultCode = 1001
    private var areaCode = ""
    // 0:点击手机号找回进入 1:点击邮箱找回进入
    private var formId = 0
    // 注册类型
    private var type = Constant.MOBIL_TYPE
    private var accountName = ""
    private var areaList: MutableList<AreaBean>? = null

    override fun getContainerView(): Int {
        return R.layout.activity_retrieve_password
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(resources.getString(R.string.retrievePassword))
        initData()
        initView()
    }

    private fun initData() {
        formId = intent.getIntExtra("formId", 0)
    }

    private fun initView() {
        when (formId) {
            0 -> {
                setAreaList()
                type = Constant.MOBIL_TYPE
                retrievePasswordEditLayout.visibility = View.VISIBLE
                emailEdit.visibility = View.GONE
                retrievePasswordTipsText.text = resources.getString(R.string.phoneNumber)
                phoneNumbEdit.addTextChangedListener { text ->
                    nextBtn.isEnabled = text!!.isNotEmpty()
                }
            }
            1 -> {
                emailView()
            }
        }
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
                        areaNumb.text = areaCode
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
                if (jsonArray == null || jsonArray.length() == 0 ) {
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
                        areaNumb.text = areaCode
                    }
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
            }
        }, tag = this)
    }

    private fun emailView() {
        type = Constant.EMAIL_TYPE
        retrievePasswordEditLayout.visibility = View.GONE
        emailEdit.visibility = View.VISIBLE
        retrievePasswordTipsText.text = resources.getString(R.string.emailNumber)
        emailEdit.addTextChangedListener { text ->
            nextBtn.isEnabled = text!!.isNotEmpty()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == areaResultCode) {
                val areaBean =
                    data?.getSerializableExtra(AreaSelectActivity.selectedArea) as AreaBean
                areaCode = "+" + areaBean.code_Tel
                areaNumb.text = areaCode
            }
        }
    }

    fun areaSelect(view: View) {
        val intent = Intent(this, AreaSelectActivity::class.java)
        if (!areaList.isNullOrEmpty()) {
            intent.putExtra(AreaSelectActivity.areas, areaList as Serializable)
        }
        startActivityForResult(intent, areaResultCode)
    }

    fun startNext(view: View) {
        if (!nextBtn.isEnabled) {
            return
        }
        val params = HttpParams()
        if (type == Constant.EMAIL_TYPE) {
            if (emailEdit.text.isEmpty()) {
                errorTips.text = resources.getString(R.string.emailTips)
                return
            }
            accountName = emailEdit.text.toString().trim()
        } else {
            if (areaCode.isEmpty()) {
                errorTips.text = resources.getString(R.string.areaTips)
                return
            }
            if (phoneNumbEdit.text.isEmpty()) {
                errorTips.text = resources.getString(R.string.phoneTips)
                return
            }
            accountName = phoneNumbEdit.text.toString().trim()
            params.put("AreaCode", areaCode)
        }
        showProgressDialog()
        params.put("RegType", type)
        params.put("AccountName", accountName)
        HttpRequest.post(RequestUrls.ACCOUNT_CHECK, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgressDialog()
                if (response == null) {
                    return
                }
                if (response.optInt("uid", 0) > 0) {
                    val intent = Intent(this@RetrievePassword, CaptchaActivity::class.java)
                    intent.putExtra("type", type)
                    intent.putExtra("areaCode", areaCode)
                    intent.putExtra("accountName", accountName)
                    startActivity(intent)
                    finish()
                } else {
                    ToastUtils.showLong(R.string.disableAccount)
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgressDialog()
                errorTips.text = errorMsg
            }

        }, params, this)
    }
}
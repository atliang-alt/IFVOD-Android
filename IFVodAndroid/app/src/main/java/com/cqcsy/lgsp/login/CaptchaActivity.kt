package com.cqcsy.lgsp.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.library.network.HttpRequest
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_captcha.*
import kotlinx.android.synthetic.main.activity_captcha.errorTips
import org.json.JSONObject
import java.lang.StringBuilder

/**
 * 图形验证码
 */
class CaptchaActivity : NormalActivity(), View.OnKeyListener {
    private val editList: MutableList<EditText> = ArrayList()
    // 回传给服务端的
    private var cookieKey = ""
    private var type = 0
    private var areaCode = ""
    private var accountName = ""

    override fun getContainerView(): Int {
        return R.layout.activity_captcha
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(resources.getString(R.string.retrievePassword))
        initView()
        initData()
    }

    private fun initData() {
        type = intent.getIntExtra("type", 0)
        areaCode = intent.getStringExtra("areaCode") ?: ""
        accountName = intent.getStringExtra("accountName") ?: ""
        getImageCode()
    }

    private fun initView() {
        editList.add(captchaEditOne)
        editList.add(captchaEditTwo)
        editList.add(captchaEditThree)
        editList.add(captchaEditFour)
        captchaEditOne.isFocusable = true
        captchaEditOne.isFocusableInTouchMode = true
        captchaEditOne.requestFocus()
        captchaEditOne.addTextChangedListener(EditTextWatcher())
        captchaEditTwo.addTextChangedListener(EditTextWatcher())
        captchaEditThree.addTextChangedListener(EditTextWatcher())
        captchaEditFour.addTextChangedListener(EditTextWatcher())

        captchaEditOne.setOnKeyListener(this)
        captchaEditTwo.setOnKeyListener(this)
        captchaEditThree.setOnKeyListener(this)
        captchaEditFour.setOnKeyListener(this)
    }

    /**
     * 获取图片验证码
     */
    private fun getImageCode() {
        progressContainer.visibility = View.VISIBLE
        captchaImage.visibility = View.GONE
        ImageUtil.showCircleAnim(imageProgress)
        HttpRequest.post(RequestUrls.GET_IMAGE_CODE, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                ImageUtil.closeCircleAnim(imageProgress)
                if (response == null) {
                    return
                }
                progressContainer.visibility = View.GONE
                captchaImage.visibility = View.VISIBLE
                cookieKey = response.getString("key")
                val bitmap = ImageUtil.base64ToImage(response.getString("pageByte"))
                captchaImage.setImageBitmap(bitmap)
            }

            override fun onError(response: String?, errorMsg: String?) {
                ImageUtil.closeCircleAnim(imageProgress)
                errorTips.text = errorMsg
            }
        }, tag = this)
    }

    fun refreshImage(view: View) {
        getImageCode()
    }

    inner class EditTextWatcher : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s!!.isNotEmpty()) {
                focus()
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }
    }

    override fun onKey(view: View?, keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DEL && event?.action == KeyEvent.ACTION_DOWN && editList.size > 0) {
            for (i in editList.indices) {
                if (editList[i].id == view?.id) {
                    if (i >= 1) {
                        editList[i].setText("")
                        editList[i - 1].isCursorVisible = true
                        editList[i - 1].requestFocus()
                    } else {
                        editList[i].setText("")
                        editList[i].isCursorVisible = true
                    }
                    break
                }
            }
            return true
        }
        return false
    }

    /**
     * 焦点处理
     */
    private fun focus() {
        var editText: EditText
        //利用for循环找出前面还没被输入字符的EditText
        for (i in editList.indices) {
            editText = editList[i]
            if (editText.text.isEmpty()) {
                editText.requestFocus()
                return
            } else {
                editText.isCursorVisible = false
            }
        }
        val lastEditText = editList[editList.size - 1]
        if (lastEditText.text.isNotEmpty()) {
            //收起软键盘 并不允许编辑 同时将输入的文本提交
            getResponse(lastEditText)
            hideSoftKey()
        }
    }

    private fun getResponse(lastEditText: EditText) {
        val stringBuilder = StringBuilder()
        for (i in editList.indices) {
            stringBuilder.append(editList[i].text.toString())
        }
        showProgressDialog()
        val params = HttpParams()
        params.put("ValidateCode", stringBuilder.toString())
        params.put("key", cookieKey)
        HttpRequest.post(RequestUrls.JUDGE_IMAGE_CODES, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                lastEditText.isCursorVisible = false
                val intent = Intent(this@CaptchaActivity, AccountVerificationActivity::class.java)
                intent.putExtra("formId", 0)
                intent.putExtra("type", type)
                intent.putExtra("areaCode", areaCode)
                intent.putExtra("accountName", accountName)
                startActivity(intent)
                dismissProgressDialog()
                finish()
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgressDialog()
                lastEditText.isCursorVisible = true
                errorTips.text = errorMsg
            }
        }, params, this)
    }

    private fun hideSoftKey() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)
    }
}

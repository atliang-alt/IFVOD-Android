package com.cqcsy.lgsp.main.mine

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import androidx.core.text.isDigitsOnly
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.utils.GlobalValue
import kotlinx.android.synthetic.main.activity_set_user_info.*

/**
 * 设置昵称、设置签名
 */
class SetUserInfoActivity : NormalActivity() {
    // 0:设置昵称、1:设置签名
    private var formType: Int = 0
    private var counts: Int = 0
    private var maxInput = 30

    override fun getContainerView(): Int {
        return R.layout.activity_set_user_info
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
        initView()
    }

    private fun initData() {
        formType = intent.getIntExtra("formType", 0)
    }

    private fun initView() {
        setRightText(R.string.preservation)
        if (formType == 1) {
            setHeaderTitle(R.string.setSign)
            maxInput = 30
            textTips.visibility = View.GONE
            if (!GlobalValue.userInfoBean?.introduce.isNullOrEmpty()) {
                editText.setText(GlobalValue.userInfoBean?.introduce)
            } else {
                editText.hint = StringUtils.getString(R.string.setSignTips)
            }
        } else {
            setHeaderTitle(R.string.setNick)
            maxInput = 16
            editText.setText(GlobalValue.userInfoBean?.nickName)
        }
        editText.filters = arrayOf(InputFilter.LengthFilter(maxInput))
        editText.setSelection(editText.text.length)
        counts = editText.text.length
        textCount.text = (maxInput - counts).toString()
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int, count: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
                counts = s.length
                textCount.text = (maxInput - s.length).toString()
            }
        })
    }

    override fun onRightClick(view: View) {
        val text = editText.text.toString().trim()
        val intent = Intent()
        if (formType == 1) {
            if (text == GlobalValue.userInfoBean?.introduce) {
                finish()
                return
            }
            intent.putExtra("introduce", text)
        } else {
            if (text.isEmpty() || text.length > 16 || text.isDigitsOnly()) {
                ToastUtils.showLong(R.string.nickNoRuleTips)
                return
            }
            if (text == GlobalValue.userInfoBean?.nickName) {
                finish()
            }
            intent.putExtra("nickname", text)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
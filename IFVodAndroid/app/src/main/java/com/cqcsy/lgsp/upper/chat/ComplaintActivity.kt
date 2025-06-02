package com.cqcsy.lgsp.upper.chat

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.library.network.HttpRequest
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_complaint.*
import org.json.JSONObject

/**
 * 投诉
 */
class ComplaintActivity : NormalActivity() {
    // 选择的值
    private var selectValue = -1
    var userId = 0

    override fun getContainerView(): Int {
        return R.layout.activity_complaint
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.complaint)
        userId = intent.getIntExtra("userId", 0)
        ImageUtil.loadCircleImage(this, intent.getStringExtra("userImage"), userImage)
        nickName.text = intent.getStringExtra("nickName")
        setClick()
    }

    private fun setClick() {
        politics.setOnClickListener {
            radioTwo.clearCheck()
            radioThree.clearCheck()
            submit.isEnabled = true
            selectValue = 0
        }
        pornographic.setOnClickListener {
            radioTwo.clearCheck()
            radioThree.clearCheck()
            submit.isEnabled = true
            selectValue = 1
        }
        maliciousHarassment.setOnClickListener {
            radioOne.clearCheck()
            radioThree.clearCheck()
            submit.isEnabled = true
            selectValue = 2
        }
        advertising.setOnClickListener {
            radioOne.clearCheck()
            radioThree.clearCheck()
            submit.isEnabled = true
            selectValue = 3
        }
        other.setOnClickListener {
            radioTwo.clearCheck()
            radioOne.clearCheck()
            submit.isEnabled = true
            selectValue = 4
        }
    }

    fun submit(view: View) {
        if (selectValue == -1) {
            ToastUtils.showLong(R.string.complaintReason)
        }
        val params = HttpParams()
        params.put("ToUID", userId)
        params.put("InfoType", selectValue)
        params.put("Reason", editContext.text.toString().trim())
        HttpRequest.post(RequestUrls.COMPLAINT, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response != null) {
                    finish()
                }
                ToastUtils.showLong(R.string.complaintSuccess)
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }

        }, params, this)
    }
}
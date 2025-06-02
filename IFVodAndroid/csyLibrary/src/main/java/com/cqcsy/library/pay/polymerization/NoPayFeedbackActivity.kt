package com.cqcsy.library.pay.polymerization

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.library.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.pay.PayUrls
import com.cqcsy.library.pay.model.NoPayReasonBean
import com.cqcsy.library.utils.JumpUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_no_pay_feedback.*
import org.json.JSONObject

/**
 * 无法付款原因反馈
 */
class NoPayFeedbackActivity : NormalActivity() {
    var reasonList: MutableList<NoPayReasonBean> = ArrayList()
    private var selectPosition = 0
    var order = ""
    var payType = ""
    override fun getContainerView(): Int {
        return R.layout.activity_no_pay_feedback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.noPayTitle)
        setRightImage(R.mipmap.icon_chat_service)
        order = intent.getStringExtra("order") ?: ""
        payType = intent.getStringExtra("payType") ?: ""
        initView()
        getReason()
    }

    override fun onRightClick(view: View) {
//        val intent = Intent(this, ChatActivity::class.java)
//        intent.putExtra(ChatActivity.CHAT_TYPE, ChatActivity.TYPE_SERVER_TGC)
//        startActivity(intent)
        val params = JumpUtils.appendJumpParam(
            "com.cqcsy.lgsp.upper.chat.ChatActivity",
            mutableMapOf("chatType" to 2),
            true
        )
        JumpUtils.jumpAnyUtils(this, params)
    }

    private fun initView() {
        val manager = GridLayoutManager(this, 2)
        manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position == (reasonList.size - 1)) {
                    2
                } else {
                    1
                }
            }
        }
        recyclerView.layoutManager = manager
        recyclerView.adapter = object : BaseQuickAdapter<NoPayReasonBean, BaseViewHolder>(
            R.layout.item_no_pay_reason,
            reasonList
        ) {
            override fun convert(holder: BaseViewHolder, item: NoPayReasonBean) {
                val position = holder.adapterPosition
                holder.setText(R.id.reasonText, item.title)
                holder.getView<ImageView>(R.id.reasonSelect).isSelected = selectPosition == position
                holder.getView<ImageView>(R.id.reasonSelect).setOnClickListener {
                    notifyItemChanged(selectPosition)
                    selectPosition = position
                    notifyItemChanged(selectPosition)
                }
            }
        }
        if (selectPosition >= 0) {
            submit.isEnabled = true
        }
    }

    private fun getReason() {
        showProgress()
        HttpRequest.post(PayUrls.NO_PAY_REASON, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgress()
                if (response != null) {
                    val list = Gson().fromJson<MutableList<NoPayReasonBean>>(
                        response.optString("list"),
                        object : TypeToken<MutableList<NoPayReasonBean>>() {}.type
                    )
                    reasonList.addAll(list)
                    recyclerView.adapter?.notifyDataSetChanged()
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
                showFailed {
                    getReason()
                }
            }

        }, tag = this)
    }

    fun submit(view: View) {
        if (selectPosition >= 0) {
            showProgressDialog()
            val param = HttpParams()
            param.put("order", order)
            param.put("status", reasonList[selectPosition].id)
            param.put("remark", editContext.text.toString().trim())
            HttpRequest.post(PayUrls.NO_PAY_FEEDBACK, object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                    dismissProgressDialog()
                    setThanksView()
                }

                override fun onError(response: String?, errorMsg: String?) {
                    ToastUtils.showLong(errorMsg)
                    dismissProgressDialog()
                }
            }, param, this)
        }
    }

    private fun setThanksView() {
        submitLayout.visibility = View.GONE
        thanksLayout.visibility = View.VISIBLE
        continuePay.text = StringUtils.getString(R.string.continue_pay, payType)
    }

    fun continuePay(view: View) {
        backOption(0)
    }

    fun goToManOption(view: View) {
        backOption(1)
    }

    // 0：继续返回支付页  1：去往人工支付
    private fun backOption(type: Int) {
        val intent = Intent()
        intent.putExtra("type", type)
        setResult(RESULT_OK, intent)
        finish()
    }
}
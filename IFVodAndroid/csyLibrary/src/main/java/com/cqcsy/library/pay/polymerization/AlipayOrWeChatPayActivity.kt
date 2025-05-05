package com.cqcsy.library.pay.polymerization

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Html
import android.view.View
import android.widget.ImageView
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.Utils
import com.cqcsy.library.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.pay.PayUrls
import com.cqcsy.library.pay.model.AlipayOrderBean
import com.cqcsy.library.pay.model.VipClassifyBean
import com.cqcsy.library.pay.model.VipPayBean
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.library.utils.JumpUtils
import com.google.gson.Gson
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_alipay_wechat_pay.*
import kotlinx.android.synthetic.main.layout_alipay_pay_success.*
import kotlinx.android.synthetic.main.layout_frozen_pay.*
import kotlinx.android.synthetic.main.layout_pay_create_order.*
import kotlinx.android.synthetic.main.layout_seller_complaint.*
import org.json.JSONObject

/**
 * 微信、支付宝 支付方式
 */
class AlipayOrWeChatPayActivity : NormalActivity() {
    var vipPayBean: VipPayBean? = null
    var vipClassifyBean: VipClassifyBean? = null
    var toUid: Int = 0
    var toAccount: String = ""
    var toUserName: String = ""
    var toNickName: String = ""
    var pathInfo: String = ""
    var orderBean: AlipayOrderBean? = null
    private var notPayMoneyCode = 1000
    private var notPayCode = 1001
    private var countDownTimer: CountDownTimer? = null
    private var isEmptyPay: Boolean = false

    override fun getContainerView(): Int {
        return R.layout.activity_alipay_wechat_pay
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vipPayBean = intent.getSerializableExtra("vipPayBean") as VipPayBean
        vipClassifyBean = intent.getSerializableExtra("vipClassifyBean") as VipClassifyBean
        toUid = intent.getIntExtra("toUid", 0)
        toAccount = intent.getStringExtra("toAccount") ?: ""
        toUserName = intent.getStringExtra("toUserName") ?: ""
        toNickName = intent.getStringExtra("toNickName") ?: ""
        pathInfo = intent.getStringExtra("pathInfo") ?: ""
        setHeaderTitle(StringUtils.getString(R.string.pay_type, (vipPayBean?.title)))
        setRightImage(R.mipmap.icon_chat_service)
        initView()
        createOrder()
    }

    private fun initView() {
        if (vipClassifyBean?.disrmb.isNullOrEmpty()) {
            money.text = vipClassifyBean?.rmb
        } else {
            money.text = vipClassifyBean?.disrmb
        }
        ImageUtil.loadImage(this, vipPayBean?.img, payImage)
        ImageUtil.loadImage(this, vipPayBean?.img, paySuccessImage)
    }

    private fun createOrder() {
        showProgress()
        val params = HttpParams()
        val promotions = vipClassifyBean?.promotions
        if (!promotions.isNullOrEmpty()) {
            params.put("Promotions", promotions.joinToString(separator = "|") {
                it.promotionCode
            })
        }
        params.put("ProductID", vipClassifyBean?.packageId ?: 0)
        params.put("SysName", vipPayBean?.payType ?: -1)
        if (toUid == 0) {
            toUid = GlobalValue.userInfoBean?.id ?: 0
        }
        params.put("ToUID", toUid)
        params.put("refer", pathInfo)
        HttpRequest.post(PayUrls.CREATE_TGC_ORDER, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgress()
                orderBean = Gson().fromJson(response.toString(), AlipayOrderBean::class.java)
                if (orderBean?.u_status == 0 && (orderBean?.account.isNullOrEmpty() || orderBean?.accountName.isNullOrEmpty())) {
                    setErrorView()
                    return
                }
                changeView()
            }

            override fun onError(response: String?, errorMsg: String?) {
                if (!errorMsg.isNullOrEmpty()) {
                    finish()
                    ToastUtils.showLong(errorMsg)
                }
                dismissProgress()
            }

        }, params, tag = this)
    }

    private fun changeView() {
        if (orderBean != null) {
            when (orderBean!!.u_status) {
                // 已经创建了订单，还在等待支付
                1 -> setPaySuccessView()
                // 卖家申诉
                2 -> setComplaintView()
                // 冻结、关闭支付
                3, 4 -> setFrozenView()
                // 创建订单
                else -> setAccountView()
            }
        }
    }

    private fun setErrorView() {
        isEmptyPay = true
        errorLayout.visibility = View.VISIBLE
        payLayout.visibility = View.GONE
        paySuccessLayout.visibility = View.GONE
        complaintLayout.visibility = View.GONE
        frozenLayout.visibility = View.GONE
        surePayLayout.visibility = View.GONE
        cancelLayout.visibility = View.GONE
    }

    /**
     * 支付
     */
    private fun setAccountView() {
        errorLayout.visibility = View.GONE
        payLayout.visibility = View.VISIBLE
        paySuccessLayout.visibility = View.GONE
        complaintLayout.visibility = View.GONE
        frozenLayout.visibility = View.GONE
        surePayLayout.visibility = View.GONE
        cancelLayout.visibility = View.GONE
        money.text = orderBean?.payPrice.toString()
        payNext.text = StringUtils.getString(R.string.payNext, 15)
        payTime.text = Html.fromHtml(getString(R.string.pay_time, "15"))
        payName.text = orderBean?.accountName
        payAccount.text = orderBean?.account
        ImageUtil.loadImage(
            this,
            orderBean?.image,
            payTipImg,
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        )
        copyAccount.setOnClickListener {
            val cm: ClipboardManager = Utils.getApp().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText(Utils.getApp().packageName, orderBean?.account))
            ToastUtils.showLong(R.string.copy_success)
        }
        countDownTimer = object : CountDownTimer(15000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                payNext.text = getString(R.string.payNext, (millisUntilFinished / 1000).toInt())
            }

            override fun onFinish() {
                payNext.text = getString(R.string.payNextNotNumb)
                payNext.isEnabled = true
            }
        }
        countDownTimer?.start()
    }

    /**
     * 支付确认
     */
    private fun setPaySureView() {
        errorLayout.visibility = View.GONE
        payLayout.visibility = View.GONE
        paySuccessLayout.visibility = View.GONE
        complaintLayout.visibility = View.GONE
        frozenLayout.visibility = View.GONE
        surePayLayout.visibility = View.VISIBLE
        cancelLayout.visibility = View.GONE
    }

    /**
     * 支付成功、等待中
     */
    private fun setPaySuccessView() {
        errorLayout.visibility = View.GONE
        payLayout.visibility = View.GONE
        paySuccessLayout.visibility = View.VISIBLE
        complaintLayout.visibility = View.GONE
        frozenLayout.visibility = View.GONE
        surePayLayout.visibility = View.GONE
        cancelLayout.visibility = View.GONE
        ImageUtil.loadImage(
            this,
            orderBean?.image,
            paySuccessTipImg,
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        )
        paySuccessName.text = orderBean?.accountName
        paySuccessAccount.text = orderBean?.account
    }

    /**
     * 卖家申诉
     */
    private fun setComplaintView() {
        errorLayout.visibility = View.GONE
        payLayout.visibility = View.GONE
        paySuccessLayout.visibility = View.GONE
        complaintLayout.visibility = View.VISIBLE
        frozenLayout.visibility = View.GONE
        surePayLayout.visibility = View.GONE
        cancelLayout.visibility = View.GONE
        complaintMoney.text = orderBean?.payPrice.toString()
        complaintType.text = getString(R.string.pay_type, vipPayBean?.title)
        complaintName.text = getString(R.string.pay_type, orderBean?.accountName)
    }

    /**
     * 支付被冻结或关闭
     */
    private fun setFrozenView() {
        errorLayout.visibility = View.GONE
        payLayout.visibility = View.GONE
        paySuccessLayout.visibility = View.GONE
        complaintLayout.visibility = View.GONE
        frozenLayout.visibility = View.VISIBLE
        surePayLayout.visibility = View.GONE
        cancelLayout.visibility = View.GONE
        if (orderBean?.u_status == 4) {
            // 关闭
            payStatus.text = getString(R.string.pay_close)
            payStatusTips.text = getString(R.string.other_pay)
            payClickTips.text = getString(R.string.click_tip_two)
        } else {
            // 冻结
            payStatus.text = getString(R.string.pay_frozen)
            payStatusTips.text = getString(R.string.other_pay_tip)
            payClickTips.text = getString(R.string.click_tip_one)
        }
    }

    /**
     * 订单取消成功
     */
    private fun setCancelSuccessView() {
        errorLayout.visibility = View.GONE
        payLayout.visibility = View.GONE
        paySuccessLayout.visibility = View.GONE
        complaintLayout.visibility = View.GONE
        frozenLayout.visibility = View.GONE
        surePayLayout.visibility = View.GONE
        cancelLayout.visibility = View.VISIBLE
    }

    override fun onRightClick(view: View) {
        if (orderBean != null) {
            if (isEmptyPay) {
                startChat(-1)
            } else {
                startChat(orderBean!!.u_status)
            }
        }
    }

    private fun startChat(status: Int) {
        val giftDays = vipClassifyBean?.giftDays ?: 0
        val giftDay = if (giftDays > 0) {
            String.format("+%d天", giftDays)
        } else {
            ""
        }
        val vipName = vipClassifyBean?.name + "(" +
                StringUtils.getString(R.string.days, vipClassifyBean?.validityDays) + giftDay + ")"
        val userNameRaw = GlobalValue.userInfoBean?.userNameRaw ?: ""
        val nickName = GlobalValue.userInfoBean?.nickName ?: ""
        val payType = vipPayBean?.title ?: ""
        val getPayType =
            if (orderBean!!.payType == 0) getString(R.string.alipay) else getString(R.string.wechat)
        val price = orderBean!!.payPrice.toString() + getString(R.string.moneyUnit)
        val payName = orderBean!!.accountName
        val payAccount = orderBean!!.account
        val vipDate = if (GlobalValue.isVipUser()) {
            TimeUtils.date2String(
                TimeUtils.string2Date(GlobalValue.userInfoBean!!.eDate, "yyyy-MM-dd\'T\'HH:mm:ss\'Z\'"),
                "yyyy-MM-dd"
            )
        } else {
            ""
        }
        val messageModel = when (status) {
            // 正常状态消息
            0 -> if (toAccount.isEmpty()) {
                StringUtils.getString(
                    R.string.pay_message,
                    vipName,
                    userNameRaw,
                    nickName,
                    vipDate,
                    payType,
                    payName,
                    payAccount
                )
            } else {
                StringUtils.getString(
                    R.string.pay_message_help,
                    toAccount, vipName, toUserName, toNickName, payType, payName, payAccount
                )
            }
            // 申诉状态消息
            2 -> if (toAccount.isEmpty()) {
                StringUtils.getString(
                    R.string.complaint_message,
                    vipName, userNameRaw, nickName, vipDate, payType, getPayType, payName, price
                )
            } else {
                StringUtils.getString(
                    R.string.complaint_message_help,
                    toAccount, vipName, toUserName, toNickName, payType, getPayType, payName, price
                )
            }
            // 冻结状态消息
            3 -> if (toAccount.isEmpty()) {
                StringUtils.getString(
                    R.string.frozen_pay_message, vipName, userNameRaw, nickName, vipDate, payType
                )
            } else {
                StringUtils.getString(
                    R.string.frozen_pay_message_help,
                    toAccount, vipName, toUserName, toNickName, payType
                )
            }
            // 关闭状态消息
            4 -> if (toAccount.isEmpty()) {
                StringUtils.getString(
                    R.string.close_pay_message, vipName, userNameRaw, nickName, vipDate, payType
                )
            } else {
                StringUtils.getString(
                    R.string.close_pay_message_help, toAccount, vipName, toUserName, toNickName, payType
                )
            }
            // 前往人工支付消息
            5 -> if (toAccount.isEmpty()) {
                StringUtils.getString(
                    R.string.artificial_pay_message, vipName, userNameRaw, nickName, vipDate
                )
            } else {
                StringUtils.getString(
                    R.string.artificial_pay_message_help, toAccount, vipName, toUserName, toNickName
                )
            }

            else -> ""
        }
//        val intent = Intent(this, ChatActivity::class.java)
//        intent.putExtra(ChatActivity.SEND_MODEL_MESSAGE, messageModel)
//        intent.putExtra(
//            ChatActivity.CHAT_TYPE,
//            if (status == 5) ChatActivity.TYPE_SERVER_CHARGE else ChatActivity.TYPE_SERVER_TGC
//        )
//        startActivity(intent)
        val params = JumpUtils.appendJumpParam(
            "com.cqcsy.lgsp.upper.chat.ChatActivity",
            mutableMapOf(
                "chatType" to if (status == 5) 3 else 2,
                "sendModelMessage" to messageModel
            ),
            true
        )
        JumpUtils.jumpAnyUtils(this, params)
    }

//    override fun onBackPressed() {
//        setResult(RESULT_OK)
//        finish()
//    }

    private fun sureOrCancelOrderHttp(status: Int) {
        showProgressDialog()
        val param = HttpParams()
        param.put("order", orderBean?.order)
        param.put("status", status)
        HttpRequest.post(PayUrls.SURE_CANCEL_ORDER, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgressDialog()
                if (status == 2) {
                    // 确定支付订单
                    setPaySuccessView()
                } else {
                    // 取消成功
                    setCancelSuccessView()
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
                dismissProgressDialog()
            }
        }, param, this)
    }

    /**
     * 我已付款，下一步
     */
    fun nextClick(view: View) {
        setPaySureView()
    }

    /**
     * 重新选择
     */
    fun reselect(view: View) {
        finish()
    }

    /**
     * 确认已付款
     */
    fun surePay(view: View) {
        if (orderBean != null) {
            sureOrCancelOrderHttp(2)
        }
    }

    /**
     * 无法支付
     */
    fun noPayClick(view: View) {
        val intent = Intent(this, NoPayFeedbackActivity::class.java)
        intent.putExtra("order", orderBean?.order)
        intent.putExtra("payType", vipPayBean?.title)
        startActivityForResult(intent, notPayCode)
    }

    /**
     * 我未付款
     */
    fun noPaySuccessClick(view: View) {
        val intent = Intent(this, NotPayMoneyActivity::class.java)
        intent.putExtra("order", orderBean?.order)
        intent.putExtra("payType", vipPayBean?.title)
        startActivityForResult(intent, notPayMoneyCode)
    }

    /**
     * 取消订单
     */
    fun cancelOrder(view: View) {
        if (orderBean != null) {
            sureOrCancelOrderHttp(1)
        }
    }

    fun backClick(view: View) {
//        val intent = Intent(this, MainActivity::class.java)
//        intent.putExtra("position", 2)
//        startActivity(intent)

        JumpUtils.jumpAnyUtils(this, JumpUtils.appendJumpParam("com.cqcsy.lgsp.main.MainActivity", mutableMapOf("position" to 2)))
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == notPayCode) {
                val type = data?.getIntExtra("type", 0)
                if (type == 1) {
                    // 前往人工支付
                    startChat(5)
                    finish()
                } else {
                    // 继续支付， 重新调用接口
                    if (countDownTimer != null) {
                        countDownTimer!!.cancel()
                        countDownTimer = null
                    }
                    createOrder()
                }
            } else {
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    override fun onDestroy() {
        if (countDownTimer != null) {
            countDownTimer!!.cancel()
            countDownTimer = null
        }
        super.onDestroy()
    }
}
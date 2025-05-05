package com.cqcsy.lgsp.vip

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.view.WindowManager
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.base.WebViewActivity
import com.cqcsy.lgsp.bean.ActivityStatusBean
import com.cqcsy.lgsp.bean.VipCategoryBean
import com.cqcsy.library.pay.model.VipClassifyBean
import com.cqcsy.library.pay.model.VipPayBean
import com.cqcsy.lgsp.vip.view.FreeSuccessDialog
import com.cqcsy.lgsp.vip.view.HorizontalVipView
import com.cqcsy.library.base.BaseActivity
import com.cqcsy.library.base.NormalFragment
import com.cqcsy.library.network.H5Address
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.views.TipsDialog
import com.lzy.okgo.model.HttpParams
import com.shuyu.gsyvideoplayer.utils.CommonUtil
import kotlinx.android.synthetic.main.layout_fragment_invite_for_vip.*
import org.json.JSONObject

/**
 ** 2023/7/6
 ** des：邀请用户砍价
 **/

class InviteForVIPFragment : NormalFragment(), OnClickListener, IGetSelectedInfo {

    companion object {
        const val STATUS_ACTIVITY = "status_activity"
        const val REQUEST_ACTIVITY_PAY = 2000

        fun newInstance(vipCategoryBean: VipCategoryBean, statusBean: ActivityStatusBean?, selectId: String?): InviteForVIPFragment {
            val fragment = InviteForVIPFragment()
            val bundle = Bundle()
            bundle.putSerializable(NormalVipFragment.CATEGORY_INFO, vipCategoryBean)
            bundle.putSerializable(STATUS_ACTIVITY, statusBean)
            bundle.putSerializable(NormalVipFragment.SELECT_ID, selectId)
            fragment.arguments = bundle
            return fragment
        }
    }

    var categoryBean: VipCategoryBean? = null
    var statusBean: ActivityStatusBean? = null
        set(value) {
            field = value
            setActivityStatus(statusBean)
        }
    var selectId: String? = null

    override var listener: OnPayItemClickListener? = null

    override fun getSelectVip(): VipClassifyBean? {
        return vip_classify.getSelectedClassify()
    }

    override fun getSelectPayType(): VipPayBean? {
        return vip_pay_type.getSelectedPayType()
    }

    override fun getContainerView(): Int {
        return R.layout.layout_fragment_invite_for_vip
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categoryBean = arguments?.getSerializable(NormalVipFragment.CATEGORY_INFO) as VipCategoryBean?
        statusBean = arguments?.getSerializable(STATUS_ACTIVITY) as ActivityStatusBean?
        selectId = arguments?.getString(NormalVipFragment.SELECT_ID)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (mIsFragmentVisible) {
            listener?.showBottom(false)
        }
        right_button.setOnClickListener(this)
        left_button.setOnClickListener(this)
        setAgreement()
        setVipCategory()
        setPayType()
        setActivityStatus(statusBean)
    }

    override fun onDestroy() {
        stopCount()
        super.onDestroy()
    }

    private fun setAgreement() {
        val agreement = invite_agreement.text
        val spannableString = SpannableStringBuilder(agreement)
        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                WebViewActivity.load(requireContext(), H5Address.INVITE_AGREEMNT)
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText = false
                ds.color = ColorUtils.getColor(R.color.blue)
            }

        }, 14, agreement.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        invite_agreement.movementMethod = LinkMovementMethod.getInstance()
        invite_agreement.text = spannableString
    }

    private fun setVipCategory() {
        vip_classify.setOnItemSelectListener(object : HorizontalVipView.OnItemSelectListener {
            override fun onItemSelect(vipClassifyBean: VipClassifyBean) {
                listener?.onSelectClassify(vipClassifyBean)
            }
        })
        if (categoryBean != null && !categoryBean?.data.isNullOrEmpty()) {
            vip_classify.setView(VipCategory.creator(categoryBean!!.styleType), categoryBean?.data!!, selectId)
        }
    }

    private fun setPayType() {
        if (categoryBean == null) return
        vip_pay_type.setData(categoryBean!!.payTypes)
    }

    private fun setActivityStatus(statusBean: ActivityStatusBean?) {
        if (!isSafe()) return
//        0 未开始 1 进行中 2 待支付
        var selectedItem: VipClassifyBean? = null
        if ((statusBean?.type ?: 0) > 0 && statusBean?.categoryId == categoryBean?.id && !categoryBean!!.data.isNullOrEmpty()) {
            for (item in categoryBean!!.data!!) {
                if (item.packageId == statusBean?.packageId) {
                    selectedItem = item
                    break
                }
            }
        }
        when (statusBean?.type) {
            1 -> selectedItem?.let { setActivityInfo(it) }
            2 -> selectedItem?.let { showResult(it, statusBean) }
            else -> setToNormal()
        }
    }

    /**
     * 未开始砍价
     */
    private fun setToNormal() {
        if (statusBean?.type == 2) {
            listener?.refreshPackage()
        }
        statusBean?.reset()
        stopCount()
        if (mIsFragmentVisible) {
            listener?.onActivityStart(statusBean)
            listener?.showBottom(false)
        }
        setVipCategory()
        result_group.isVisible = false
        left_button.isVisible = false
        tip_group.isVisible = true
        pay_type_group.isVisible = false
        activity_tip.isVisible = true
        right_button.isVisible = true
        val buttonParams = button_container.layoutParams as ConstraintLayout.LayoutParams
        buttonParams.height = SizeUtils.dp2px(44f)
        buttonParams.topMargin = SizeUtils.dp2px(30f)
        buttonParams.width = ConstraintLayout.LayoutParams.MATCH_PARENT
        button_container.layoutParams = buttonParams
        right_button.setText(R.string.invite_friend)
    }

    /**
     * 砍价进行中
     */
    private fun setActivityInfo(selectedItem: VipClassifyBean) {
        val layoutParams = vip_classify.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.width = ConstraintLayout.LayoutParams.WRAP_CONTENT
        vip_classify.layoutParams = layoutParams
        vip_classify.setView(VipCategory.creator(categoryBean!!.styleType), arrayListOf(selectedItem), selectId)
        right_button.setText(R.string.to_activity_web)
    }

    /**
     * 砍价结束页面
     */
    private fun showResult(selectedItem: VipClassifyBean, statusBean: ActivityStatusBean) {
        result_group.isVisible = true
        activity_tip.isVisible = false
        tip_group.isVisible = false

        val layoutParams = vip_classify.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.width = ConstraintLayout.LayoutParams.WRAP_CONTENT
        vip_classify.layoutParams = layoutParams
        vip_classify.setView(VipCategory.creator(categoryBean!!.styleType), arrayListOf(selectedItem), selectId)

        val buttonParams = button_container.layoutParams as ConstraintLayout.LayoutParams
        buttonParams.height = SizeUtils.dp2px(36f)
        countDown(lose_efficacy_time, (statusBean.endTime ?: 0) * 1000)
        left_button.isVisible = true
        if (statusBean.price == 0F) {
            buttonParams.topMargin = SizeUtils.dp2px(40f)
            right_button.setText(R.string.confirm_get_vip)
            setResultTips(getString(R.string.free_vip_tip), 4, 6)
        } else {
            pay_type_group.isVisible = true
            right_button.isVisible = false
            buttonParams.topMargin = SizeUtils.dp2px(10f)
            buttonParams.width = SizeUtils.dp2px(150f)
            val price = selectedItem.priceSymbol + statusBean.price.toString()
            setResultTips(getString(R.string.invite_result_buy_price, price), 8, 8 + price.length)
        }
        if (mIsFragmentVisible) {
            if (statusBean.price == 0F) {
                listener?.showBottom(false)
            } else {
                listener?.showBottom(true)
                listener?.onSelectClassify(selectedItem)
            }
        }
        button_container.layoutParams = buttonParams
    }

    private fun setResultTips(tip: String, start: Int, end: Int) {
        val freeTip = SpannableStringBuilder(tip)
        freeTip.setSpan(ForegroundColorSpan(ColorUtils.getColor(R.color.red)), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        cut_off_result.text = freeTip
    }


    var mCountDownTimer: CountDownTimer? = null
    private fun countDown(textView: TextView, time: Long) {
        stopCount()
        mCountDownTimer = object : CountDownTimer(time, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                textView.text = getString(R.string.lose_efficacy_time, CommonUtil.stringForTime(millisUntilFinished))
            }

            override fun onFinish() {
                textView.isVisible = false
                setToNormal()
            }

        }
        mCountDownTimer?.start()
    }

    private fun stopCount() {
        mCountDownTimer?.cancel()
        mCountDownTimer = null
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.left_button -> showGiveUpTip()
            R.id.right_button -> {
                // 1、未发起活动或活动进行中->跳转活动页
                // 2、活动结束->免费领取
                when (statusBean?.type) {
                    0 -> getActivityAgreement()
                    1 -> showActivityPage()
                    2 -> freeVip()
                }
            }
        }
    }

    private fun showGiveUpTip() {
        val dialog = TipsDialog(requireContext()).apply {
            setMsg(R.string.give_up_tip)
            setDialogTitle(R.string.give_up)
            setLeftListener(R.string.cancel) {
                dismiss()
            }
            setRightListener(R.string.give_up) {
                dismiss()
                giveUp()
            }
        }
        dialog.show()
    }

    /**
     * 获取活动协议
     */
    private fun getActivityAgreement() {
        (requireActivity() as BaseActivity).showProgressDialog(false)
        HttpRequest.post(RequestUrls.RECHARGE_ACTIVITY, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                (requireActivity() as BaseActivity).dismissProgressDialog()
                val rule = response?.optString("activityRule")
                if (response == null || rule.isNullOrEmpty()) {
                    return
                }
                showAgreement(rule)
            }

            override fun onError(response: String?, errorMsg: String?) {
                (requireActivity() as BaseActivity).dismissProgressDialog()
            }
        }, tag = this)
    }

    private fun showAgreement(agreement: String) {
        val dialog = TipsDialog(requireContext())
        dialog.setDialogTitle(R.string.inviteRules)
        dialog.setMsg(agreement)
        dialog.setGravity(Gravity.LEFT)
        dialog.setLeftListener {
            dialog.dismiss()
        }
        dialog.setRightListener(R.string.agree) {
            dialog.dismiss()
            joinActivity()
        }
        dialog.show()

        val attribute = dialog.window?.attributes
        attribute?.width = WindowManager.LayoutParams.WRAP_CONTENT
        attribute?.height = ScreenUtils.getAppScreenHeight() * 3 / 5
        dialog.window?.attributes = attribute
    }

    private fun joinActivity() {
        (requireActivity() as BaseActivity).showProgressDialog(false)
        val params = HttpParams()
        val packageId = vip_classify.getSelectedClassify()?.packageId
        packageId?.let { params.put("productID", it) }
        HttpRequest.post(RequestUrls.START_ACTIVITY, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                (requireActivity() as BaseActivity).dismissProgressDialog()
                statusBean?.key = response?.optString("key")
                statusBean?.endTime = response?.optLong("endTime")
                statusBean?.packageId = packageId
                statusBean?.categoryId = categoryBean?.id
                statusBean?.url = response?.optString("url")
                statusBean?.type = 1
                vip_classify.getSelectedClassify()?.let { setActivityInfo(it) }
                listener?.onActivityStart(statusBean)
                showActivityPage()
            }

            override fun onError(response: String?, errorMsg: String?) {
                (requireActivity() as BaseActivity).dismissProgressDialog()
            }

        }, params, tag = this)
    }

    private fun showActivityPage() {
        if (statusBean?.url.isNullOrEmpty() || (statusBean?.type ?: 0) == 0) return
        val intent = Intent(context, WebViewActivity::class.java)
        intent.putExtra(WebViewActivity.urlKey, statusBean?.url)
        startActivityForResult(intent, REQUEST_ACTIVITY_PAY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_ACTIVITY_PAY) {
            listener?.refreshStatus()
        }
    }

    private fun freeVip() {
        if (statusBean?.key.isNullOrEmpty()) return

        (requireActivity() as BaseActivity).showProgressDialog(false)
        val params = HttpParams()
        statusBean?.key?.let { params.put("key", it) }
        HttpRequest.get(RequestUrls.FREE_VIP, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                (requireActivity() as BaseActivity).dismissProgressDialog()

                FreeSuccessDialog.show(requireActivity())
                setToNormal()
            }

            override fun onError(response: String?, errorMsg: String?) {
                (requireActivity() as BaseActivity).dismissProgressDialog()
                ToastUtils.showShort(errorMsg)
            }

        }, params, tag = this)
    }

    private fun giveUp() {
        if (statusBean?.key.isNullOrEmpty()) return

        (requireActivity() as BaseActivity).showProgressDialog(false)
        val params = HttpParams()
        statusBean?.key?.let { params.put("key", it) }
        HttpRequest.get(RequestUrls.GIVE_UP_ACTIVITY, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                (requireActivity() as BaseActivity).dismissProgressDialog()

                setToNormal()
            }

            override fun onError(response: String?, errorMsg: String?) {
                (requireActivity() as BaseActivity).dismissProgressDialog()
                ToastUtils.showShort(errorMsg)
            }

        }, params, tag = this)
    }
}
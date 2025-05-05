package com.cqcsy.lgsp.vip

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.blankj.utilcode.util.StringUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.VipCategoryBean
import com.cqcsy.library.pay.model.VipClassifyBean
import com.cqcsy.library.pay.model.VipPayBean
import com.cqcsy.lgsp.vip.view.HorizontalVipView
import com.cqcsy.library.base.BaseFragment
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.views.TipsDialog
import kotlinx.android.synthetic.main.layout_fragment_sale_off_vip.*
import org.json.JSONObject

/**
 ** 2023/7/6
 ** des：VIP分享折扣活动
 **/

class SaleOffVIPFragment : BaseFragment(), OnClickListener, IGetSelectedInfo {

    companion object {

        fun newInstance(vipCategoryBean: VipCategoryBean, selectId: String?): SaleOffVIPFragment {
            val fragment = SaleOffVIPFragment()
            val bundle = Bundle()
            bundle.putSerializable(NormalVipFragment.CATEGORY_INFO, vipCategoryBean)
            bundle.putSerializable(NormalVipFragment.SELECT_ID, selectId)
            fragment.arguments = bundle
            return fragment
        }
    }

    var shareText: String? = null
    var isCopy: Boolean = false

    var categoryBean: VipCategoryBean? = null
    var selectId: String? = null

    override var listener: OnPayItemClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categoryBean = arguments?.getSerializable(NormalVipFragment.CATEGORY_INFO) as VipCategoryBean?
        selectId = arguments?.getString(NormalVipFragment.SELECT_ID)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_fragment_sale_off_vip, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        copy.setOnClickListener(this)
        confirm_share.setOnClickListener(this)
        purchase.setOnClickListener(this)

        setVipCategory()
        setPayType()
        getRechargeActivity()
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

    override fun onResume() {
        super.onResume()
        if (isCopy) {
            confirm_share.isEnabled = true
        }
    }

    private fun getRechargeActivity() {
        HttpRequest.post(RequestUrls.RECHARGE_ACTIVITY, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                val remark = response.optString("qrCodeRemark")
                val title = response.optString("qrCodeTitle")
                val shareText = response.optString("shareText")
                this@SaleOffVIPFragment.shareText = shareText
                purchase.text = StringUtils.getString(R.string.discount_purchase, title)
                tv_vip_discount.text = title
                share_text.text = shareText
                tv_vip_discount_desc.text = remark
            }

            override fun onError(response: String?, errorMsg: String?) {

            }
        }, tag = this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.copy -> {
                if (!shareText.isNullOrEmpty()) {
                    showCopySuccessDialog()
                    val cm = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText(requireActivity().packageName, shareText))
                    isCopy = true
                }
            }

            R.id.confirm_share -> {
                share_container.isVisible = false
                share_success_container.isVisible = true
            }

            R.id.purchase -> {
                share_root.isVisible = false
                line.isVisible = true
                vip_pay_type.isVisible = true
                listener?.showBottom(true)
            }
        }
    }

    private fun showCopySuccessDialog() {
        val dialog = TipsDialog(requireContext())
        dialog.setDialogTitle(R.string.copySuccess)
        dialog.setMsg(R.string.copy_success_share_vip_tip)
        dialog.setRightListener(R.string.known) {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun getSelectVip(): VipClassifyBean? {
        return vip_classify.getSelectedClassify()
    }

    override fun getSelectPayType(): VipPayBean? {
        return vip_pay_type.getSelectedPayType()
    }

    override fun isShared(): Boolean {
        return vip_pay_type.isVisible
    }
}
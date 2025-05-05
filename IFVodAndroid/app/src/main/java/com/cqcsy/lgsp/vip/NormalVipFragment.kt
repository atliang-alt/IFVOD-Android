package com.cqcsy.lgsp.vip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.VipCategoryBean
import com.cqcsy.library.pay.model.VipClassifyBean
import com.cqcsy.library.pay.model.VipPayBean
import com.cqcsy.lgsp.vip.view.HorizontalVipView
import com.cqcsy.library.base.BaseFragment
import kotlinx.android.synthetic.main.layout_fragment_normal_vip.vip_classify
import kotlinx.android.synthetic.main.layout_fragment_normal_vip.vip_pay_type

/**
 ** 2023/7/6
 ** des：原价购买
 **/

class NormalVipFragment : BaseFragment(), IGetSelectedInfo {

    companion object {
        val CATEGORY_INFO = "category_info"
        val SELECT_ID = "select_id"

        fun newInstance(vipCategoryBean: VipCategoryBean, selectId: String?): NormalVipFragment {
            val fragment = NormalVipFragment()
            val bundle = Bundle()
            bundle.putSerializable(CATEGORY_INFO, vipCategoryBean)
            bundle.putSerializable(SELECT_ID, selectId)
            fragment.arguments = bundle
            return fragment
        }
    }

    var categoryBean: VipCategoryBean? = null
    var selectId: String? = null

    override var listener: OnPayItemClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categoryBean = arguments?.getSerializable(CATEGORY_INFO) as VipCategoryBean?
        selectId = arguments?.getString(SELECT_ID)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_fragment_normal_vip, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setVipCategory()
        setPayType()
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

    override fun getSelectVip(): VipClassifyBean? {
        return vip_classify.getSelectedClassify()
    }

    override fun getSelectPayType(): VipPayBean? {
        return vip_pay_type.getSelectedPayType()
    }
}
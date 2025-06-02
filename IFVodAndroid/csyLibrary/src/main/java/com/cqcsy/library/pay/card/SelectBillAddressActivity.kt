package com.cqcsy.library.pay.card

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.library.R
import com.cqcsy.library.base.WebViewActivity
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.pay.PayUrls
import com.cqcsy.library.pay.model.BillAddressBean
import com.cqcsy.library.pay.model.CardOrderBean
import com.cqcsy.library.pay.model.VipClassifyBean
import com.cqcsy.library.pay.model.VipPayBean
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.views.TipsDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_select_bill_address.*
import org.json.JSONObject

/**
 * 选择信用卡账单地址
 */
class SelectBillAddressActivity : NormalActivity() {
    private var addressList: MutableList<BillAddressBean> = ArrayList()
    private var adapter: BaseQuickAdapter<BillAddressBean, BaseViewHolder>? = null
    private val addBillAddressCode = 1000
    private val editBillAddressCode = 1001
    private val webCode = 1002

    private var selectPosition: Int = 0

    private var mOrderId = -1

    private var pathInfo = ""

    override fun getContainerView(): Int {
        return R.layout.activity_select_bill_address
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.selectBillAddress)
        pathInfo = intent.getStringExtra("pathInfo") ?: ""
        initView()
        getCreditAddress()
    }

    private fun initView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = object : BaseQuickAdapter<BillAddressBean, BaseViewHolder>(
            R.layout.item_bill_address,
            addressList
        ) {
            override fun convert(holder: BaseViewHolder, item: BillAddressBean) {
                val position = holder.adapterPosition
                holder.setText(R.id.fullName, item.firstname + " " + item.lastname)
                holder.setText(R.id.address, item.address + "，" + item.city + "，" + item.stateName + "，" + item.countryName)
                holder.setText(R.id.postalCode, item.zipCode + "，" + item.city + "，" + item.countryName)
                holder.setText(R.id.phoneNumb, item.phone)
                holder.setText(R.id.email, item.email)
                holder.getView<ImageView>(R.id.selectImg).isSelected = selectPosition == position
                holder.getView<ImageView>(R.id.selectImg).setOnClickListener {
                    notifyItemChanged(selectPosition)
                    selectPosition = position
                    notifyItemChanged(selectPosition)
                }
                holder.getView<ImageView>(R.id.deleteImg).setOnClickListener {
                    deleteDialog(holder.adapterPosition, item)
                }
                holder.getView<ImageView>(R.id.editImg).setOnClickListener {
                    val intent = Intent(this@SelectBillAddressActivity, AddBillAddressActivity::class.java)
                    intent.putExtra(AddBillAddressActivity.positionKey, position)
                    intent.putExtra(AddBillAddressActivity.billAddressBeanKey, item)
                    startActivityForResult(intent, editBillAddressCode)
                }
            }
        }
        adapter?.addFooterView(addFooter())
        recyclerView.adapter = adapter
        emptyAddLayout.setOnClickListener {
            startActivityForResult(Intent(this, AddBillAddressActivity::class.java), addBillAddressCode)
        }
    }

    private fun getCreditAddress() {
        showProgressDialog()
        HttpRequest.get(PayUrls.GET_CREDIT_CARD_ADDRESS, callBack = object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgressDialog()
                if (response != null && response?.optJSONArray("list")?.length() ?: 0 > 0) {
                    val list = Gson().fromJson<MutableList<BillAddressBean>>(
                        response.optJSONArray("list")!!.toString(), object : TypeToken<MutableList<BillAddressBean>>() {}.type
                    )
                    addressList.addAll(list)
                    adapter?.notifyDataSetChanged()
                    next.visibility = View.VISIBLE
                } else {
                    dataLayout.visibility = View.GONE
                    noDataLayout.visibility = View.VISIBLE
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                showFailed {
                    getCreditAddress()
                }
            }
        }, tag = this)
    }

    private fun deleteDialog(position: Int, item: BillAddressBean) {
        val tipsDialog = TipsDialog(this)
        tipsDialog.setDialogTitle(R.string.tips)
        tipsDialog.setMsg(R.string.deleteBillAddress)
        tipsDialog.setLeftListener(R.string.cancel) {
            tipsDialog.dismiss()
        }
        tipsDialog.setRightListener(R.string.delete) {
            deleteAddress(position, item)
            tipsDialog.dismiss()
        }
        tipsDialog.show()
    }

    private fun deleteAddress(position: Int, item: BillAddressBean) {
        showProgressDialog()
        val params = HttpParams()
        params.put("AddressID", item.id)
        HttpRequest.post(PayUrls.DELETE_PAY_ADDRESS, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgressDialog()
                addressList.remove(item)
                if (selectPosition > 0 && position <= selectPosition) {
                    selectPosition--
                }
                adapter?.notifyDataSetChanged()
                if (addressList.isEmpty()) {
                    dataLayout.visibility = View.GONE
                    noDataLayout.visibility = View.VISIBLE
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgressDialog()
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                editBillAddressCode -> {
                    val serializable = data?.getSerializableExtra(AddBillAddressActivity.billAddressBeanKey)
                    val position = data?.getIntExtra(AddBillAddressActivity.positionKey, -1) ?: -1
                    if (serializable != null && position != -1) {
                        addressList[position] = serializable as BillAddressBean
                        adapter?.notifyDataSetChanged()
                    }
                }

                addBillAddressCode -> {
                    val serializable = data?.getSerializableExtra(AddBillAddressActivity.billAddressBeanKey)
                    if (serializable != null) {
                        addressList.add(0, serializable as BillAddressBean)
                        adapter?.notifyDataSetChanged()
                        if (addressList.size == 1) {
                            dataLayout.visibility = View.VISIBLE
                            noDataLayout.visibility = View.GONE
                        }
                        next.isVisible = true
                    }
                }

                webCode -> {
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
    }

    private fun addFooter(): View {
        val footer = View.inflate(this, R.layout.layout_add_bill_address, null)
        footer.setOnClickListener {
            startActivityForResult(Intent(this, AddBillAddressActivity::class.java), addBillAddressCode)
        }
        return footer
    }

    fun nextClick(view: View) {
        if (mOrderId == -1) {
            createOrder()
        } else {
            showProgressDialog(false)
            getOrder(mOrderId)
        }
    }

    private fun createOrder() {
        showProgressDialog(false)
        val params = HttpParams()
        val vipPayBean: VipPayBean = intent.getSerializableExtra("vipPayBean") as VipPayBean
        val vipClassifyBean: VipClassifyBean =
            intent.getSerializableExtra("vipClassifyBean") as VipClassifyBean
        var toUid: Int = intent.getIntExtra("toUid", 0)
        val promotions = vipClassifyBean.promotions
        if (!promotions.isNullOrEmpty()) {
            params.put("Promotions", promotions.joinToString(separator = "|") {
                it.promotionCode
            })
        }
        params.put("ProductID", vipClassifyBean.packageId)
        params.put("SysName", vipPayBean.payType)
        if (toUid == 0) {
            toUid = GlobalValue.userInfoBean?.id ?: 0
        }
        params.put("ToUID", toUid)
        params.put("refer", pathInfo)
        HttpRequest.post(PayUrls.CREATE_CARD_ORDER, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response != null) {
                    val bean = Gson().fromJson<CardOrderBean>(
                        response.toString(), object : TypeToken<CardOrderBean>() {}.type
                    )
                    mOrderId = bean.orderID
                    getOrder(bean.orderID)
                } else {
                    dismissProgressDialog()
                    ToastUtils.showLong(R.string.order_create_failed)
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgressDialog()
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    private fun getOrder(orderId: Int) {
        val params = HttpParams()
        params.put("addressid", addressList[selectPosition].id)
        params.put("orderid", orderId)
        HttpRequest.post(PayUrls.GET_CARD_ORDER, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgressDialog()
                if (response != null && !response.isNull("gateway") && !response.isNull("params")) {
                    // 跳转支付H5页面
                    val intent = Intent(this@SelectBillAddressActivity, WebViewActivity::class.java)
                    intent.putExtra(WebViewActivity.urlKey, response.optString("gateway"))
                    intent.putExtra(WebViewActivity.postParams, response.optString("params"))
                    intent.putExtra(WebViewActivity.titleKey, StringUtils.getString(R.string.credit_card_pay))
                    startActivityForResult(intent, webCode)
                } else {
                    ToastUtils.showLong(R.string.order_params_error)
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgressDialog()
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }
}
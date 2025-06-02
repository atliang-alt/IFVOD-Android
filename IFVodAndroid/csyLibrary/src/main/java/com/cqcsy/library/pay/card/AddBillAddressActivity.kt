package com.cqcsy.library.pay.card

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.library.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.bean.AreaBean
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.pay.PayUrls
import com.cqcsy.library.pay.area.AreaSelectActivity
import com.cqcsy.library.pay.area.ProvinceSelectActivity
import com.cqcsy.library.pay.model.BillAddressBean
import com.cqcsy.library.pay.model.ProvinceBean
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_add_bill_address.*
import org.json.JSONObject
import java.io.Serializable

/**
 * 添加/修改信用卡账单地址
 */
class AddBillAddressActivity : NormalActivity() {
    companion object {
        const val billAddressBeanKey = "billAddressBean"
        const val positionKey = "position"
    }

    private var billAddressBean: BillAddressBean? = null
    private var position: Int = -1
    private var mProvinceList: MutableList<ProvinceBean>? = null

    override fun getContainerView(): Int {
        return R.layout.activity_add_bill_address
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun initView() {
        val serializable = intent.getSerializableExtra(billAddressBeanKey)
        if (serializable != null) {
            setHeaderTitle(R.string.modify_bill_address)
            position = intent.getIntExtra(positionKey, -1)
            billAddressBean = serializable as BillAddressBean
            nameEdit.setText(billAddressBean?.firstname)
            lastNameEdit.setText(billAddressBean?.lastname)
            country.text = billAddressBean?.countryName
            country.tag = billAddressBean?.country
            province.text = billAddressBean?.stateName
            province.tag = billAddressBean?.state
            cityEdit.setText(billAddressBean?.city)
            postalCodeEdit.setText(billAddressBean?.zipCode)
            addressEdit.setText(billAddressBean?.address)
            phoneEdit.setText(billAddressBean?.phone)
            emailEdit.setText(billAddressBean?.email)
        } else {
            setHeaderTitle(R.string.add_bill_address)
        }
        setBtuEnabled()
        nameEdit.addTextChangedListener(listener)
        lastNameEdit.addTextChangedListener(listener)
        cityEdit.addTextChangedListener(listener)
        postalCodeEdit.addTextChangedListener(listener)
        addressEdit.addTextChangedListener(listener)
        phoneEdit.addTextChangedListener(listener)
        emailEdit.addTextChangedListener(listener)
    }

    private val listener: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable) {
            setBtuEnabled()
        }
    }

    private fun setBtuEnabled() {
        save.isEnabled = nameEdit.text.isNotEmpty() && lastNameEdit.text.isNotEmpty()
                && country.text.isNotEmpty() && cityEdit.text.isNotEmpty()
                && postalCodeEdit.text.isNotEmpty() && addressEdit.text.isNotEmpty()
                && phoneEdit.text.isNotEmpty() && emailEdit.text.isNotEmpty()
    }

    fun selectCountry(view: View) {
        val intent = Intent(this, AreaSelectActivity::class.java)
        intent.putExtra(AreaSelectActivity.selectedEnglish, true)
        startActivityForResult(intent, 1000)
    }

    fun selectProvince(view: View?) {
        if (mProvinceList.isNullOrEmpty()) {
            if (country.tag != null) {
                getProvince(country.tag.toString(), true)
            } else {
                ToastUtils.showShort(R.string.select_country_first)
            }
        } else {
            selectProvince()
        }
    }

    fun selectProvince() {
        if (mProvinceList.isNullOrEmpty()) {
            ToastUtils.showShort(R.string.empty_province_tip)
            return
        }
        val intent = Intent(this, ProvinceSelectActivity::class.java)
        intent.putExtra(ProvinceSelectActivity.PROVINCE_DATA, mProvinceList as Serializable)
        startActivityForResult(intent, 1001)
    }

    fun saveClick(view: View) {
        if (billAddressBean == null) {
            billAddressBean = BillAddressBean()
        }
        billAddressBean!!.firstname = nameEdit.text.toString()
        billAddressBean!!.lastname = lastNameEdit.text.toString()
        billAddressBean!!.address = addressEdit.text.toString()
        if (country.tag != null) {
            billAddressBean!!.country = country.tag.toString()
        }
        if (province.tag != null) {
            billAddressBean!!.state = province.tag.toString()
        }
        billAddressBean!!.city = cityEdit.text.toString()
        billAddressBean!!.zipCode = postalCodeEdit.text.toString()
        billAddressBean!!.phone = phoneEdit.text.toString()
        billAddressBean!!.email = emailEdit.text.toString()
        updateAddress(billAddressBean!!)
    }

    private fun updateAddress(bill: BillAddressBean) {
        showProgressDialog(false)
        val params = HttpParams()
        params.put("id", bill.id)
        params.put("Firstname", bill.firstname)
        params.put("Lastname", bill.lastname)
        params.put("Country", bill.country)
        params.put("state", bill.state)
        params.put("City", bill.city)
        params.put("Address", bill.address)
        params.put("Phone", bill.phone)
        params.put("Email", bill.email)
        params.put("ZipCode", bill.zipCode)
        HttpRequest.post(PayUrls.GET_PAY_ADDRESS, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgressDialog()
                if (response != null) {
                    val bean = Gson().fromJson<BillAddressBean>(
                        response.toString(),
                        object : TypeToken<BillAddressBean>() {}.type
                    )
                    val intent = Intent()
                    intent.putExtra(billAddressBeanKey, bean)
                    intent.putExtra(positionKey, position)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgressDialog()
                ToastUtils.showShort(errorMsg)
            }
        }, params, tag = this)
    }

    private fun getProvince(countryCode: String, needSelect: Boolean = false) {
        mProvinceList?.clear()
        HttpRequest.cancelRequest(PayUrls.PROVINCE_LIST)
        val params = HttpParams()
        params.put("Code", countryCode)
        HttpRequest.get(PayUrls.PROVINCE_LIST, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response != null && !response.isNull("list")) {
                    mProvinceList = Gson().fromJson<MutableList<ProvinceBean>>(
                        response.optJSONArray("list").toString(),
                        object : TypeToken<MutableList<ProvinceBean>>() {}.type
                    )
                    if (needSelect) {
                        selectProvince()
                    }
                } else {
                    ToastUtils.showShort(R.string.province_list_failed)
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showShort(R.string.province_list_failed)
            }
        }, params, PayUrls.PROVINCE_LIST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1000) {
                val serializable = data?.getSerializableExtra("selectedArea")
                if (serializable is AreaBean) {
                    country.tag = serializable.code
                    country.text = serializable.english
                    province.tag = null
                    province.text = null
                    getProvince(serializable.code)
                    setBtuEnabled()
                }
            } else if (requestCode == 1001) {
                val serializable = data?.getSerializableExtra("selectedArea")
                if (serializable is ProvinceBean) {
                    province.tag = serializable.stateCode
                    province.text = serializable.stateName
                }
            }
        }
    }
}
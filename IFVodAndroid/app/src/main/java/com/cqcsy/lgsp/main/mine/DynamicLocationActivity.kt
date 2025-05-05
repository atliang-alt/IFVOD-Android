package com.cqcsy.lgsp.main.mine

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.baidu.location.BDLocation
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.BuildConfig
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.lgsp.bean.GeoCodeResultBean
import com.cqcsy.lgsp.utils.Location
import com.cqcsy.lgsp.utils.NormalUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import kotlinx.android.synthetic.main.activity_dynamic_location.*
import org.json.JSONObject

/**
 * 动态定位
 */
class DynamicLocationActivity : NormalActivity() {
    companion object {
        const val ADDRESS = "address"
        const val DETAILED_ADDRESS = "detailedAddress"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
    }

    private var locationCity = ""
    private var locationCountry = ""
    private var latitude = 0.00
    private var longitude = 0.00
    private var resultList: MutableList<GeoCodeResultBean>? = null
    private var adapter: BaseQuickAdapter<GeoCodeResultBean, BaseViewHolder>? = null
    private var address = ""
    private var detail = ""

    override fun getContainerView(): Int {
        return R.layout.activity_dynamic_location
    }

    override fun onViewCreate() {
        super.onViewCreate()
        setHeaderTitle(R.string.location)
        setRightImage(R.mipmap.icon_search)
        failedLargeTip.text = StringUtils.getString(R.string.location_fail_tips)
        failedLittleTip.visibility = View.GONE
        rightImageView.visibility = View.GONE
        address = intent.getStringExtra(ADDRESS) ?: ""
        detail = intent.getStringExtra(DETAILED_ADDRESS) ?: ""
        resultList = ArrayList()
        initView()
    }

    override fun onStart() {
        super.onStart()
        requestPermissions()
    }

    private fun requestPermissions() {
        val permissionUtils = NormalUtil.getLocationPermissionRequest()
        permissionUtils.callback(object : PermissionUtils.SimpleCallback {
            override fun onGranted() {
                startLocation()
            }

            override fun onDenied() {
                ToastUtils.showLong(R.string.permission_location)
            }
        })
        permissionUtils.request()
    }

    private fun startLocation() {
        showProgress()
        val location = Location.instance(this)
        location.resultListener = object : Location.OnLocationListener {
            override fun onResult(location: BDLocation) {
                latitude = location.latitude
                longitude = location.longitude
                locationCity = location.city
                locationCountry = location.country
                rightImageView.visibility = View.VISIBLE
                geoCode()
            }

            override fun onError(errorMsg: String) {
                showFailed {
                    startLocation()
                }
            }

        }
        location.start()
    }

    override fun onRightClick(view: View) {
//        if (latitude == 0.00 || longitude == 0.00) {
//            return
//        }
        val intent = Intent(this, LocationSearchActivity::class.java)
//        intent.putExtra("locationLatLng", "${latitude},${longitude}")
//        intent.putExtra("locationCity", locationCity)
//        intent.putExtra("locationCountry", locationCountry)
        startActivityForResult(intent, 1000)
    }

    private fun initView() {
        if (address.isEmpty()) {
            oldLocation.visibility = View.GONE
            oldLocationLine.visibility = View.GONE
            noLocationImg.visibility = View.VISIBLE
        } else {
            oldLocation.visibility = View.VISIBLE
            oldLocationLine.visibility = View.VISIBLE
            oldLocationImg.visibility = View.VISIBLE
            oldLocationName.text = address
        }
        noLocation.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }
        oldLocation.setOnClickListener {
            setResult(RESULT_OK, intent)
            finish()
        }
        adapter = object :
            BaseQuickAdapter<GeoCodeResultBean, BaseViewHolder>(
                R.layout.item_poi_search_result,
                resultList
            ) {
            override fun convert(holder: BaseViewHolder, item: GeoCodeResultBean) {
                holder.setText(R.id.resultName, item.formatted_address)
                holder.getView<LinearLayout>(R.id.itemLayout).setOnClickListener {
                    val intent = Intent()
                    intent.putExtra(ADDRESS, item.formatted_address)
                    intent.putExtra(DETAILED_ADDRESS, item.formatted_address)
                    intent.putExtra(LATITUDE, item.geometry?.location?.lat)
                    intent.putExtra(LONGITUDE, item.geometry?.location?.lng)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    /**
     * 请求谷歌附近搜索接口
     */
    private fun geoCode() {
        val locationLatLng = "${latitude},${longitude}"
        val url =
            "https://maps.googleapis.com/maps/api/geocode/json?latlng=$locationLatLng&key=${BuildConfig.GOOGLE_MAP_KEY}"
        OkGo.post<String>(url).tag(this)
            .execute(object : StringCallback() {
                override fun onSuccess(response: Response<String>) {
                    dismissProgress()
                    val resStr = response.body().toString()
                    if (resStr.isEmpty()) {
                        return
                    }
                    val jsonObject = JSONObject(resStr.trim())
                    val jsonArray = jsonObject.optJSONArray("results")
                    if (jsonArray == null || jsonArray.length() == 0) {
                        return
                    }
                    for (i in 0 until jsonArray.length()) {
                        val beanJson = jsonArray.optString(i)
                        val poiResultBean = Gson().fromJson<GeoCodeResultBean>(
                            beanJson, object : TypeToken<GeoCodeResultBean>() {}.type
                        )
                        val list = poiResultBean.types?.filter { it.contains("plus_code") }
                        if (!list.isNullOrEmpty()) {
                            continue
                        }
                        val address = poiResultBean.formatted_address.replace(locationCountry, "")
                            .replace(locationCity, "")
                        if (address.isEmpty()) {
                            poiResultBean.formatted_address = "$locationCountry·$locationCity"
                        } else {
                            poiResultBean.formatted_address =
                                "$locationCountry·$locationCity·$address"
                        }
                        if ((resultList?.filter { it.formatted_address == poiResultBean.formatted_address }).isNullOrEmpty()) {
                            resultList?.add(poiResultBean)
                        }
                    }
                    adapter?.notifyDataSetChanged()
                }

                override fun onError(response: Response<String>) {
                    dismissProgress()
                }
            })
    }

    override fun onBackPressed() {
        if (latitude != 0.00 && longitude != 0.00) {
            val intent = Intent()
            intent.putExtra(LATITUDE, latitude)
            intent.putExtra(LONGITUDE, longitude)
            setResult(RESULT_OK, intent)
        }
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1000) {
                // 位置选择
                address = data?.getStringExtra(ADDRESS) ?: ""
                val latitude = data?.getDoubleExtra(LATITUDE, 0.00) ?: 0.00
                val longitude = data?.getDoubleExtra(LONGITUDE, 0.00) ?: 0.00
                detail = data?.getStringExtra(DETAILED_ADDRESS) ?: ""
                val intent = Intent()
                intent.putExtra(ADDRESS, address)
                intent.putExtra(DETAILED_ADDRESS, detail)
                intent.putExtra(LATITUDE, latitude)
                intent.putExtra(LONGITUDE, longitude)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }

}
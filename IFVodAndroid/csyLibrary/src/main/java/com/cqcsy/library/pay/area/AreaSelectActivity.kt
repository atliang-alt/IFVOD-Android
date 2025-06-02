package com.cqcsy.library.pay.area

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.SPUtils
import com.cqcsy.library.R
import com.cqcsy.library.bean.AreaBean
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.bean.AreaGroupBean
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.pay.PayUrls
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.views.WaveSideBar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.layout_header_list.*
import org.json.JSONObject

/**
 * 选择区域和区号
 */
class AreaSelectActivity : NormalActivity() {

    companion object {
        const val selectedArea = "selectedArea"
        const val areas = "areas"
        const val selectedEnglish = "selectedEnglish"
    }

    private var isEnglish = false
    private lateinit var areaAdapter: AreaSelectAdapter
    override fun getContainerView(): Int {
        return R.layout.layout_header_list
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.area_select)
        isEnglish = intent.getBooleanExtra(selectedEnglish, false)
        val areaList = intent.getSerializableExtra(areas) as? MutableList<AreaBean>
        initListView()
        if (areaList != null) {
            sortArea(areaList)
        } else {
            setAreaList()
        }
    }

    private fun initListView() {
        val layoutManager = LinearLayoutManager(this)
        sideBar.setOnSelectIndexItemListener(object : WaveSideBar.OnSelectIndexItemListener {
            override fun onSelectIndexItem(index: String) {
                var position = 0
                val groups = areaAdapter.groups
                for (i in groups.indices) {
                    if (groups[i].letter == index) {
                        layoutManager.scrollToPositionWithOffset(position, 0)
                        break
                    } else if (index == "#") {
                        layoutManager.scrollToPositionWithOffset(0, 0)
                    } else {
                        groups[i].countries?.let { position += it.size + 1 }
                    }
                }
            }
        })
        list.layoutManager = layoutManager
        areaAdapter = AreaSelectAdapter(this, isEnglish)
        areaAdapter.setOnChildClickListener { _, _, groupPosition, childPosition ->
            val bean = areaAdapter.groups[groupPosition].countries?.get(childPosition)
            if (bean != null) {
                val intent = Intent()
                intent.putExtra(selectedArea, bean)
                setResult(Activity.RESULT_OK, intent)
            }
            finish()
        }
        list.adapter = areaAdapter
    }

    private fun setAreaList() {
        val list = getLocalAreaList()
        if (list.isNullOrEmpty()) {
            getAreaList()
        } else {
            sortArea(list)
        }
    }

    private fun getLocalAreaList(): MutableList<AreaBean>? {
        val areaStr = SPUtils.getInstance().getString(Constant.KEY_COUNTRY_AREA_INFO)
        val jsonObject = if (!areaStr.isNullOrEmpty()) {
            JSONObject(areaStr)
        } else {
            JSONObject()
        }
        val saveTime = jsonObject.optLong(Constant.KEY_COUNTRY_AREA_INFO_TIME)
        val country = jsonObject.optJSONArray("country")
        if (System.currentTimeMillis() - saveTime > Constant.COUNTRY_TIME || country == null || country.length() == 0) {
            return null
        }
        return Gson().fromJson<ArrayList<AreaBean>>(
            country.toString(),
            object : TypeToken<ArrayList<AreaBean>>() {}.type
        )
    }

    private fun getCommonUserAreaList() {
        HttpRequest.get(PayUrls.COMMON_USE_COUNTRY_LIST, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val jsonArray = response?.optJSONArray("list")
                if (jsonArray == null || jsonArray.length() == 0) {
                    return
                }
                val listData = Gson().fromJson<ArrayList<AreaBean>>(
                    jsonArray.toString(),
                    object : TypeToken<ArrayList<AreaBean>>() {}.type
                )

                areaAdapter.addData(0, AreaGroupBean().apply {
                    letter = "常用地区"
                    countries = listData
                })
                sideBar.addIndexItems(0, "#")
            }

            override fun onError(response: String?, errorMsg: String?) {
                showFailed { getAreaList() }
            }
        }, tag = this)
    }

    private fun getAreaList() {
        showProgress()
        HttpRequest.get(PayUrls.COUNTRY_LIST, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgress()
                val jsonArray = response?.optJSONArray("country")
                if (jsonArray == null || jsonArray.length() == 0) {
                    return
                }
                val listData = Gson().fromJson<ArrayList<AreaBean>>(
                    jsonArray.toString(),
                    object : TypeToken<ArrayList<AreaBean>>() {}.type
                )
                response.put(Constant.KEY_COUNTRY_AREA_INFO_TIME, System.currentTimeMillis())
                SPUtils.getInstance().put(Constant.KEY_COUNTRY_AREA_INFO, response.toString())
                sortArea(listData)
            }

            override fun onError(response: String?, errorMsg: String?) {
                showFailed { getAreaList() }
            }
        }, tag = this)
    }

    private fun sortArea(list: MutableList<AreaBean>) {
        if (list.size == 0) {
            showEmpty()
        } else {
            val data: MutableList<AreaGroupBean> = ArrayList()
            val temp: MutableList<AreaBean> =
                list.filter { !it.code_Tel.isNullOrEmpty() }.toMutableList()
            val sorted = if (isEnglish) {
                temp.groupBy { it.english.substring(0, 1) }
            } else {
                temp.groupBy { it.letter }
            }
            val result = sorted.toSortedMap()

            result.forEach {
                val bean = AreaGroupBean()
                bean.letter = it.key
                bean.countries = Gson().fromJson(
                    Gson().toJson(it.value),
                    object : TypeToken<ArrayList<AreaBean>>() {}.type
                )
                data.add(bean)
            }
            if (data.isNotEmpty()) {
                setAreaList(data)
            }
        }
        getCommonUserAreaList()
    }

    private fun setAreaList(data: MutableList<AreaGroupBean>) {
        val letters = ArrayList<String>()
        for (bean in data) {
            letters.add(bean.letter)
        }
        sideBar.setIndexItems(letters)
        areaAdapter.setList(data)
    }
}
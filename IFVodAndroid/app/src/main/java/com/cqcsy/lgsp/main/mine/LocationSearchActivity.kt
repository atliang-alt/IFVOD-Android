package com.cqcsy.lgsp.main.mine

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.BuildConfig
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.AutocompleteResultBean
import com.cqcsy.library.base.BaseActivity
import com.cqcsy.lgsp.bean.TextSearchResultBean
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import kotlinx.android.synthetic.main.activity_dynamic_location_search.*
import org.json.JSONObject

/**
 * 动态位置搜索
 */
class LocationSearchActivity : BaseActivity() {
    private var resultList: MutableList<TextSearchResultBean>? = null
    private var adapter: BaseQuickAdapter<TextSearchResultBean, BaseViewHolder>? = null
    private var keyword = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dynamic_location_search)
        resultList = ArrayList()
        initView()
    }

    private fun initView() {
        searchText.setOnClickListener {
            resultList?.clear()
            adapter?.notifyDataSetChanged()
            keyword = searchEdit.text.toString().trim()
            if (keyword.isNotEmpty()) {
                poiHttpData(keyword)
            }
        }
        searchEdit.requestFocus()
        searchEdit.setOnEditorActionListener { _, actionId, _ ->
            keyword = searchEdit.text.toString().trim()
            if (actionId == EditorInfo.IME_ACTION_SEARCH && keyword.isNotEmpty()) {
                resultList?.clear()
                adapter?.notifyDataSetChanged()
                keyword = searchEdit.text.toString()
                poiHttpData(keyword)
            }
            false
        }
        adapter = object :
            BaseQuickAdapter<TextSearchResultBean, BaseViewHolder>(
                R.layout.item_poi_search_result,
                resultList
            ) {
            override fun convert(holder: BaseViewHolder, item: TextSearchResultBean) {
                holder.setText(R.id.resultName, item.name)
                holder.getView<LinearLayout>(R.id.itemLayout).setOnClickListener {
                    val intent = Intent()
                    intent.putExtra(
                        DynamicLocationActivity.ADDRESS, item.name
                    )
                    intent.putExtra(
                        DynamicLocationActivity.DETAILED_ADDRESS,
                        item.formattedAddress
                    )
                    intent.putExtra(DynamicLocationActivity.LATITUDE, item.lat)
                    intent.putExtra(DynamicLocationActivity.LONGITUDE, item.lng)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.setOnTouchListener { _, _ ->
            KeyboardUtils.hideSoftInput(searchEdit)
            false
        }
    }

    private fun poiHttpData(query: String) {
        showProgress()
        val autocompleteUrl =
            "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=$query&types=geocode&key=${BuildConfig.GOOGLE_MAP_KEY}"
//        val nearbySearchUrl =
//            "https://maps.googleapis.com/maps/api/place/nearbysearch/json?keyword=$query&location=$locationLatLng&type=establishment&radius=5000&key=${BuildConfig.GOOGLE_MAP_KEY}"
        OkGo.post<String>(autocompleteUrl).tag(this)
            .execute(object : StringCallback() {
                override fun onSuccess(response: Response<String>) {
                    dismissProgress()
                    resultList?.clear()
                    adapter?.notifyDataSetChanged()
                    val resStr = response.body().toString()
                    if (resStr.isEmpty()) {
                        showEmpty(query)
                        return
                    }
                    setNearbySearchData(query, resStr)
                    adapter?.notifyDataSetChanged()
                }

                override fun onError(response: Response<String>?) {
                    dismissProgress()
                    showEmpty(query)
                }
            })
    }

    private fun setNearbySearchData(query: String, resStr: String) {
        val jsonStr = JSONObject(resStr.trim()).optString("predictions")
        if (jsonStr.isEmpty()) {
            showEmpty(query)
            return
        }
        val list = Gson().fromJson<MutableList<AutocompleteResultBean>>(
            jsonStr,
            object : TypeToken<MutableList<AutocompleteResultBean>>() {}.type
        )
        if (list.isNullOrEmpty()) {
            resultList?.clear()
            adapter?.notifyDataSetChanged()
            showEmpty(query)
            return
        }
        recyclerView.visibility = View.VISIBLE
        noLocation.visibility = View.GONE
        list.forEach {
            val textSearchResultBean = TextSearchResultBean()
            textSearchResultBean.formattedAddress = it.description
            textSearchResultBean.lat = it.lat
            textSearchResultBean.lng = it.lng
            if (it.terms.size > 1) {
                textSearchResultBean.name = it.terms.last().value + "·" + it.terms[0].value
            } else if (it.terms.size == 1) {
                textSearchResultBean.name = it.terms[0].value
            }
            if ((resultList?.filter { result -> result.name == textSearchResultBean.name }).isNullOrEmpty()) {
                resultList?.add(textSearchResultBean)
            }
        }
    }

    private fun showEmpty(query: String) {
        recyclerView.visibility = View.GONE
        noLocation.visibility = View.VISIBLE
        noLocation.text =
            StringUtils.getString(R.string.noLocation, "\"" + query + "\"")
    }

    private fun showProgress() {
        if (isSafe()) {
            recyclerView.visibility = View.GONE
            statusView.showProgress()
        }
    }

    private fun dismissProgress() {
        if (isSafe()) {
            recyclerView.visibility = View.VISIBLE
            statusView.dismissProgress()
        }
    }

    fun clickBack(view: View) {
        finish()
    }
}
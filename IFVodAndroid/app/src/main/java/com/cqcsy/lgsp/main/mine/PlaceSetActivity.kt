package com.cqcsy.lgsp.main.mine

import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.SPUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.library.bean.AreaBean
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.utils.Constant
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import kotlinx.android.synthetic.main.layout_recyclerview.*


class PlaceSetActivity : NormalActivity() {

    val AREA_URL = "https://ppt.ifsp.tv/a/getappregions"

//    val areas = mapOf(
//        "GL" to "全球",
//        "US" to "美国",
//        "CA" to "加拿大",
//        "UK" to "英国",
//        "IT" to "意大利",
//        "DE" to "德国",
//        "FR" to "法国",
//        "ES" to "西班牙",
//        "MY" to "马来西亚",
//        "AU" to "澳洲",
//        "JP" to "日本",
//        "NZ" to "新西兰",
//        "KR" to "韩国",
//        "SG" to "新加坡"
//    )

    override fun getContainerView(): Int {
        return R.layout.layout_recyclerview
    }

    override fun onViewCreate() {
        setHeaderTitle("选择国家")
        getArea()
    }

    private fun getArea() {
        OkGo.get<String>(AREA_URL).execute(object : StringCallback() {
            override fun onSuccess(response: Response<String>?) {
                if (response?.code() == 200) {
                    val body = response.body()
                    val areaList = Gson().fromJson<MutableList<AreaBean>>(
                        body.toString(),
                        object : TypeToken<MutableList<AreaBean>>() {}.type
                    )
                    setAreaList(areaList)
                }
            }

        })
    }

    private fun setAreaList(areaList: MutableList<AreaBean>) {
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = object : BaseQuickAdapter<AreaBean, BaseViewHolder>(
            R.layout.item_center_text,
            areaList
        ) {
            override fun convert(holder: BaseViewHolder, item: AreaBean) {
                holder.setText(R.id.text, item.name)
            }

        }
        adapter.setOnItemClickListener { adapter, view, position ->
            val areaBean = areaList.get(position)
            SPUtils.getInstance().put(
                Constant.AREA_CODE,
                areaBean.code + "," + areaBean.name
            )
            finish()
        }
        recyclerView.adapter = adapter
    }
}
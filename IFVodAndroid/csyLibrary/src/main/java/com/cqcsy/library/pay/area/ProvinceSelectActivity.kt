package com.cqcsy.library.pay.area

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.cqcsy.library.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.bean.AreaGroupBean
import com.cqcsy.library.pay.model.ProvinceBean
import com.cqcsy.library.views.WaveSideBar
import kotlinx.android.synthetic.main.layout_header_list.list
import kotlinx.android.synthetic.main.layout_header_list.sideBar

/**
 ** 2023/11/30
 ** des：省份选择
 **/

class ProvinceSelectActivity : NormalActivity() {

    companion object {
        const val PROVINCE_DATA = "province_data"
    }

    override fun getContainerView(): Int {
        return R.layout.layout_header_list
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.province_select)

        val dataList = intent.getSerializableExtra(PROVINCE_DATA) as? MutableList<ProvinceBean>?
        setProvinceList(sortArea(dataList))
    }

    private fun sortArea(list: MutableList<ProvinceBean>?): MutableList<AreaGroupBean>? {
        if (list.isNullOrEmpty()) {
            showEmpty()
            return null
        } else {
            val data: MutableList<AreaGroupBean> = ArrayList()
            val sorted = list.groupBy { it.stateName?.substring(0, 1) ?: "" }
            val result = sorted.toSortedMap()
            result.forEach {
                val bean = AreaGroupBean()
                bean.letter = it.key
                bean.provinces = it.value.toMutableList()
                data.add(bean)
            }
            return data
        }
    }

    private fun setProvinceList(data: MutableList<AreaGroupBean>?) {
        val layoutManager = LinearLayoutManager(this)
        list.layoutManager = layoutManager
        val adapter = ProvinceSelectAdapter(this)
        adapter.setOnChildClickListener { adapter, holder, groupPosition, childPosition ->
            val bean = (adapter as ProvinceSelectAdapter).groups[groupPosition].provinces?.get(childPosition)
            if (bean != null) {
                val intent = Intent()
                intent.putExtra(AreaSelectActivity.selectedArea, bean)
                setResult(Activity.RESULT_OK, intent)
            }
            finish()
        }
        sideBar.setOnSelectIndexItemListener(object : WaveSideBar.OnSelectIndexItemListener {
            override fun onSelectIndexItem(index: String) {
                var position = 0
                val groups = adapter.groups
                for (i in groups.indices) {
                    if (groups[i].letter == index) {
                        layoutManager.scrollToPositionWithOffset(position, 0)
                        break
                    } else if (index == "#") {
                        layoutManager.scrollToPositionWithOffset(0, 0)
                    } else {
                        groups[i].provinces?.let { position += it.size + 1 }
                    }
                }
            }
        })
        list.adapter = adapter
        adapter.setList(data)
    }
}
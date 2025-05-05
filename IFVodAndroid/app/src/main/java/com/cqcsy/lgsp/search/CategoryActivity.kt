package com.cqcsy.lgsp.search

import android.content.Intent
import android.util.TypedValue
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ColorUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.lgsp.bean.CategoryBean
import com.cqcsy.lgsp.bean.net.CategoryNetBean
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.network.HttpRequest
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.littlejerk.rvdivider.builder.XGridBuilder
import kotlinx.android.synthetic.main.activity_category.*
import org.json.JSONObject

/**
 * 分类选择
 */
class CategoryActivity : NormalActivity() {
    var leftCategoryList: MutableList<CategoryBean> = ArrayList()
    var rightCategoryList: MutableList<CategoryBean> = ArrayList()
    var rightCategoryMap: MutableMap<String, MutableList<CategoryBean>> = HashMap()
    var selectedBean: CategoryBean? = null

    override fun getContainerView(): Int {
        return R.layout.activity_category
    }

    fun initData() {
        getHttpData()
    }

    override fun onViewCreate() {
        setHeaderTitle(R.string.category_guid)
        initData()
        initList()
    }

    private fun initList() {
        leftCategory.layoutManager = LinearLayoutManager(this)

        rightCategory.layoutManager = GridLayoutManager(this, 2)
    }

    private fun setLeftAdapter() {
        val leftAdapter = object : BaseQuickAdapter<CategoryBean, BaseViewHolder>(
            R.layout.layout_left_category_item,
            leftCategoryList
        ) {
            override fun convert(holder: BaseViewHolder, item: CategoryBean) {
                val name = holder.getView<TextView>(R.id.category_name)
                name.text = item.classifyName
                if (item.classifyId == selectedBean?.classifyId) {
                    name.setTextColor(ColorUtils.getColor(R.color.blue))
                    name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                    name.setBackgroundColor(ColorUtils.getColor(R.color.background_video_item))
                } else {
                    name.setTextColor(ColorUtils.getColor(R.color.word_color_5))
                    name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    name.setBackgroundColor(ColorUtils.getColor(R.color.background_8))
                }
                name.setOnClickListener {
                    selectedBean = item
                    leftCategory.adapter?.notifyDataSetChanged()
                    rightCategoryList.clear()
                    rightCategoryMap[selectedBean?.classifyId]?.let { it1 ->
                        rightCategoryList.addAll(
                            it1
                        )
                    }
                    rightCategory.adapter?.notifyDataSetChanged()
                }
            }

        }
        leftCategory.adapter = leftAdapter
        setRightAdapter()
    }

    private fun setRightAdapter() {
        val rightAdapter = object : BaseQuickAdapter<CategoryBean, BaseViewHolder>(
            R.layout.layout_category_right_item,
            rightCategoryList
        ) {
            override fun convert(holder: BaseViewHolder, item: CategoryBean) {
                holder.setText(R.id.right_category_name, item.classifyName)
                holder.getView<TextView>(R.id.right_category_name).setOnClickListener {
                    if (item.classType == 2) {
                        // 小视频跳转
                        val intent = Intent(context, ShortVideoFilterActivity::class.java)
                        intent.putExtra("categoryId", selectedBean?.classifyId)
                        intent.putExtra("classifyName", item.classifyName)
                        intent.putExtra("subId", item.subID)
                        startActivity(intent)
                    } else {
                        val intent = Intent(context, CategoryFilterActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                        intent.putExtra("categoryId", selectedBean?.classifyId)
                        intent.putExtra("categoryName", selectedBean?.classifyName)
                        intent.putExtra("classifyName", item.classifyName)
                        intent.putExtra("classifyId", item.classifyId)
                        intent.putExtra("classifyIndex", item.index)
                        startActivity(intent)
                    }
                }
            }

        }
        rightCategory.adapter = rightAdapter
        rightCategory.addItemDecoration(XGridBuilder(this).setVLineSpacing(10f).setHLineSpacing(10f).build())
    }

    private fun getHttpData() {
        showProgressDialog()
        HttpRequest.post(RequestUrls.CHANNEL_NAVIGATION, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgressDialog()
                val jsonArray = response?.optJSONArray("list")
                if (jsonArray == null || jsonArray.length() == 0) {
                    showEmpty()
                    return
                }
                for (i in 0 until jsonArray.length()) {
                    val categoryNetBean =
                        Gson().fromJson(jsonArray[i].toString(), CategoryNetBean::class.java)
                    val leftCategoryBean = CategoryBean()
                    leftCategoryBean.classifyId = categoryNetBean.categoryId
                    leftCategoryBean.classifyName = categoryNetBean.name
                    if (categoryNetBean.list.isNullOrEmpty()) {
                        continue
                    }
                    val rightArray =
                        JsonParser.parseString(Gson().toJson(categoryNetBean.list)).asJsonArray
                    val rightList: MutableList<CategoryBean> =
                        Gson().fromJson(
                            rightArray.toString(),
                            object : TypeToken<MutableList<CategoryBean>>() {}.type
                        )
                    leftCategoryList.add(leftCategoryBean)
                    rightCategoryMap[categoryNetBean.categoryId] = rightList
                }
                selectedBean = leftCategoryList[0]
                rightCategoryList.addAll(rightCategoryMap[selectedBean?.classifyId]!!)
                setLeftAdapter()
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgressDialog()
                showFailed {
                    getHttpData()
                }
            }
        }, tag = this)
    }
}
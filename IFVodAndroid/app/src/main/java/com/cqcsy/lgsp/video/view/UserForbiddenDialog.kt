package com.cqcsy.lgsp.video.view

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.library.bean.UserInfoBean
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.network.HttpRequest
import com.littlejerk.rvdivider.builder.XGridBuilder
import com.littlejerk.rvdivider.builder.XLinearBuilder
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_word_forbidden.*
import org.json.JSONObject

/**
 * 用户屏蔽列表
 */
class UserForbiddenDialog(context: Context) : VideoMenuDialog(context) {
    var listData: MutableList<UserInfoBean>? = null
    var listener: onRemoveForbidden? = null

    interface onRemoveForbidden {
        fun onRemove(bean: UserInfoBean)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_word_forbidden)
        wordForbiddenNum.text = context.getString(R.string.user_forbidden_num, listData?.size ?: 0)
        addWord.visibility = View.GONE
        emptyText.text = StringUtils.getString(R.string.user_empty)
        initList()
    }

    private fun initList() {
        if (listData.isNullOrEmpty()) {
            wordList.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
        } else {
            wordList.visibility = View.VISIBLE
            emptyText.visibility = View.GONE
        }
        if (isVertical) {
            wordList.layoutManager = GridLayoutManager(context, 2)
            wordList.addItemDecoration(
                XGridBuilder(context).setHLineSpacing(10f).setVLineSpacing(5f).build()
            )
            itemLayout.setPadding(
                SizeUtils.dp2px(7f),
                SizeUtils.dp2px(20f),
                SizeUtils.dp2px(7f),
                0
            )
        } else {
            wordList.layoutManager = LinearLayoutManager(context)
            wordList.addItemDecoration(XLinearBuilder(context).setSpacing(10f).build())
            itemLayout.setPadding(
                SizeUtils.dp2px(20f),
                SizeUtils.dp2px(20f),
                SizeUtils.dp2px(20f),
                0
            )
        }
        wordList.adapter = object :
            BaseQuickAdapter<UserInfoBean, BaseViewHolder>(
                R.layout.layout_word_forbidden_item,
                listData
            ) {
            override fun convert(holder: BaseViewHolder, item: UserInfoBean) {
                holder.setText(R.id.item_name, item.nickName)
                holder.getView<ImageView>(R.id.delete_item).setOnClickListener {
                    removeForbidden(item)
                }
            }
        }
    }

    private fun removeForbidden(bean: UserInfoBean) {
        val params = HttpParams()
        params.put("uid", bean.id)
        HttpRequest.post(RequestUrls.REMOVE_FORBIDDEN, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                listData = listData?.filter { it.id != bean.id }?.toMutableList()
                if (listData != null) {
                    (wordList.adapter as BaseQuickAdapter<UserInfoBean, BaseViewHolder>).setList(
                        listData
                    )
                }
                listener?.onRemove(bean)
                if (listData.isNullOrEmpty()) {
                    wordList.visibility = View.GONE
                    emptyText.visibility = View.VISIBLE
                }
            }

            override fun onError(response: String?, errorMsg: String?) {

            }
        }, params, this)
    }

    /**
     * 移除屏蔽词刷新页面标题数量
     */
    fun resumeViewSize(size: Int) {
        wordForbiddenNum.text = context.getString(R.string.user_forbidden_num, size)
    }
}
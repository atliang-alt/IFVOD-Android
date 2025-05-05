package com.cqcsy.lgsp.main.mine

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.refresh.RefreshListActivity
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.lgsp.upper.UpperInfoBean
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.lgsp.vip.util.VipGradeImageUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import org.json.JSONArray
import org.json.JSONObject

/**
 * 黑名单管理页
 */
class BlackListManager : RefreshListActivity<UpperInfoBean>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.blackListTitle)
        emptyLargeTip.text = getString(R.string.noData)
        emptyLittleTip.visibility = View.GONE
        getRecyclerView().isScrollOption = false
    }

    override fun getUrl(): String {
        return RequestUrls.GET_BLACK_TABLE
    }

    override fun getItemLayout(): Int {
        return R.layout.item_black_list
    }

    override fun parsingData(jsonArray: JSONArray): MutableList<UpperInfoBean> {
        return Gson().fromJson(
            jsonArray.toString(),
            object : TypeToken<MutableList<UpperInfoBean>>() {}.type
        )
    }

    override fun setItemView(holder: BaseViewHolder, item: UpperInfoBean, position: Int) {
        val params = if (!item.introduce.isNullOrEmpty()) {
            RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                SizeUtils.dp2px(101f)
            )
        } else {
            RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                SizeUtils.dp2px(89f)
            )
        }
        params.leftMargin = SizeUtils.dp2px(12f)
        params.rightMargin = SizeUtils.dp2px(12f)
        holder.getView<RelativeLayout>(R.id.itemLayout).layoutParams = params
        ImageUtil.loadCircleImage(this, item.avatar, holder.getView(R.id.userPhoto))
        val userVip = holder.getView<ImageView>(R.id.userVip)
        if (item.bigV || item.vipLevel > 0) {
            userVip.visibility = View.VISIBLE
            userVip.setImageResource(
                if (item.bigV) R.mipmap.icon_big_v else VipGradeImageUtil.getVipImage(
                    item.vipLevel
                )
            )
        } else {
            userVip.visibility = View.GONE
        }
        holder.setText(R.id.nickName, item.nickName)
        // 0女1男，-1没有选择
        when (item.sex) {
            1 -> {
                holder.setVisible(R.id.sexImage, true)
                holder.setImageResource(R.id.sexImage, R.mipmap.icon_sex_man_32)
            }
            0 -> {
                holder.setVisible(R.id.sexImage, true)
                holder.setImageResource(R.id.sexImage, R.mipmap.icon_sex_woman_32)
            }
            else -> {
                holder.setGone(R.id.sexImage, true)
            }
        }
        if (item.introduce.isNullOrEmpty()) {
            holder.setGone(R.id.userSign, true)
        } else {
            holder.setGone(R.id.userSign, false)
            holder.setText(R.id.userSign, item.introduce)
        }
        holder.setText(
            R.id.fansCount,
            StringUtils.getString(R.string.fansCount, NormalUtil.formatPlayCount(item.fansCount))
        )
        holder.setText(R.id.videoCount, StringUtils.getString(R.string.work_count, item.totalVideo))
        holder.getView<TextView>(R.id.removeText).setOnClickListener {
            moveBlackList(position)
        }
    }

    private fun moveBlackList(position: Int) {
        showProgressDialog()
        val params = HttpParams()
        params.put("uid", getDataList()[position].id)
        params.put("status", true)
        HttpRequest.post(RequestUrls.CHAT_FORBIDDEN, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                getDataList().removeAt(position)
                refreshView()
                dismissProgressDialog()
                if (getDataList().isEmpty()) {
                    showEmpty()
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgressDialog()
                ToastUtils.showLong(errorMsg)
            }

        }, params, this)
    }
}
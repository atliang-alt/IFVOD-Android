package com.cqcsy.lgsp.search

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.refresh.RefreshDataFragment
import com.cqcsy.lgsp.bean.ShortVideoBean
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.lgsp.video.VideoBaseActivity
import com.cqcsy.lgsp.video.VideoPlayVerticalActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.littlejerk.rvdivider.builder.XLinearBuilder
import com.lzy.okgo.model.HttpParams
import org.json.JSONArray

/**
 * 分类筛选页面 小视频的fragment
 */
class ShortVideoFilterFragment : RefreshDataFragment<ShortVideoBean>() {
    // 筛选条件ID 默认综合ID=0
    private var filterId = 0
    private var categoryId = ""
    private var subId = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        filterId = arguments?.getInt("filterId", 0) ?: 0
        categoryId = arguments?.getString("categoryId") ?: ""
        subId = arguments?.getString("subId") ?: ""
        super.onViewCreated(view, savedInstanceState)
    }

    override fun initView() {
        emptyLargeTip.text = StringUtils.getString(R.string.searchNoData)
        emptyLittleTip.text = StringUtils.getString(R.string.searchNoDataTips)
    }

    override fun addDecoration(recyclerView: RecyclerView) {
        recyclerView.addItemDecoration(
            XLinearBuilder(requireContext()).setSpacing(20f).build()
        )
    }

    override fun getUrl(): String {
        return RequestUrls.SHORT_VIDEO_FILTER
    }

    override fun getParams(): HttpParams {
        val params = HttpParams()
        params.put("titleId", categoryId)
        params.put("ids", filterId)
        params.put("name", subId)
        return params
    }

    override fun getItemLayout(): Int {
        return R.layout.item_short_video
    }

    override fun parsingData(jsonArray: JSONArray): MutableList<ShortVideoBean> {
        return Gson().fromJson(
            jsonArray.toString(),
            object : TypeToken<MutableList<ShortVideoBean>>() {}.type
        )
    }

    override fun onItemClick(position: Int, dataBean: ShortVideoBean, holder: BaseViewHolder) {
        val intent = Intent(context, VideoPlayVerticalActivity::class.java)
        intent.putExtra(VideoBaseActivity.PLAY_VIDEO_BEAN, dataBean)
        startActivity(intent)
    }

    override fun setItemView(holder: BaseViewHolder, item: ShortVideoBean, position: Int) {
        item.coverImgUrl?.let {
            ImageUtil.loadImage(
                requireContext(),
                it, holder.getView(R.id.shortVideoImage)
            )
        }
        holder.setText(R.id.shortVideoTitle, item.title)
        holder.setText(R.id.shortVideoUserName, item.upperName)
        holder.setText(R.id.times, item.duration)
        holder.setText(R.id.shortVideoPlayCount, NormalUtil.formatPlayCount(item.playCount))
        holder.setText(
            R.id.shortVideoUpdateTime,
            TimeUtils.date2String(TimesUtils.formatDate(item.date), "yyyy-MM-dd")
        )
    }
}
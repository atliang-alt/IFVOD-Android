package com.cqcsy.lgsp.search

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.DynamicBean
import com.cqcsy.lgsp.main.mine.DynamicDetailsActivity
import com.cqcsy.lgsp.upper.UpperActivity
import com.cqcsy.lgsp.utils.DynamicUtils
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.lgsp.vip.util.VipGradeImageUtil
import com.cqcsy.library.base.refresh.RefreshDataFragment
import com.cqcsy.library.utils.ImageUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import org.json.JSONArray

/**
 * 搜索动态页
 */
class SearchDynamicFragment : RefreshDataFragment<DynamicBean>() {
    private var keyword = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        keyword = arguments?.getString("keyword") ?: ""
        emptyLargeTip.text = StringUtils.getString(R.string.noSearchDynamic, "\"" + keyword + "\"")
        emptyLittleTip.text = ""
        super.onViewCreated(view, savedInstanceState)
    }

    override fun getUrl(): String {
        return RequestUrls.SEARCH_DYNAMIC
    }

    override fun getItemLayout(): Int {
        return R.layout.item_search_dynamic
    }

    override fun getParams(): HttpParams {
        val params = HttpParams()
        params.put("title", keyword)
        return params
    }

    override fun parsingData(jsonArray: JSONArray): MutableList<DynamicBean> {
        return Gson().fromJson(
            jsonArray.toString(),
            object : TypeToken<List<DynamicBean>>() {}.type
        )
    }

    override fun setItemView(holder: BaseViewHolder, item: DynamicBean, position: Int) {
        if (item.description.isNullOrEmpty()) {
            holder.setGone(R.id.item_des, true)
        } else {
            holder.setText(R.id.item_des, Html.fromHtml(item.description!!.replace("\n", "<br>")))
            holder.setGone(R.id.item_des, false)
        }
        if (!item.createTime.isNullOrEmpty()) {
            holder.setText(
                R.id.release_time, StringUtils.getString(
                    R.string.release_time, TimeUtils.date2String(
                        TimesUtils.formatDate(item.createTime ?: ""), "yyyy-MM-dd"
                    )
                )
            )
        } else {
            holder.setText(R.id.release_time, "")
        }
        if (item.address.isNullOrEmpty()) {
            holder.setGone(R.id.dynamic_location, true)
        } else {
            holder.setText(R.id.dynamic_location, item.address)
            holder.setGone(R.id.dynamic_location, false)
        }
        holder.setText(R.id.nickName, item.upperName)
        ImageUtil.loadCircleImage(requireActivity(), item.headImg, holder.getView(R.id.userPhoto))
        setVipLevel(holder.getView(R.id.userVip), item)
        holder.setText(R.id.comment_count, NormalUtil.formatPlayCount(item.comments))
        holder.setText(R.id.zan_count, NormalUtil.formatPlayCount(item.likeCount))
            .setText(R.id.view_count, NormalUtil.formatPlayCount(item.viewCount))
        val imageContainer = holder.getView<LinearLayout>(R.id.imageContainer)
        val videoContainer = holder.getView<FrameLayout>(R.id.videoContainer)
        if (item.photoType == 1) {
            imageContainer.isVisible = true
            videoContainer.isVisible = false
            DynamicUtils.addDynamicImages(
                requireActivity(),
                imageContainer,
                item.trendsDetails,
                item.photoCount,
                item.isUnAvailable
            )
        } else {
            imageContainer.isVisible = false
            videoContainer.isVisible = true
            val videoCover = holder.getView<ImageView>(R.id.iv_video_cover)
            videoCover.setImageDrawable(null)
            val size = DynamicUtils.getCoverSize(item.imageRatioValue, DynamicUtils.getCellWidth())
            videoContainer.updateLayoutParams<LinearLayout.LayoutParams> {
                this.width = size.width
                this.height = size.height
            }
            ImageUtil.loadImage(
                this,
                item.cover,
                videoCover,
                imageWidth = size.width,
                imageHeight = size.height,
                corner = 2
            )
        }
        holder.getView<ImageView>(R.id.userPhoto).setOnClickListener {
            val intent = Intent(context, UpperActivity::class.java)
            intent.putExtra(UpperActivity.UPPER_ID, item.uid)
            startActivity(intent)
        }
    }

    override fun onItemClick(position: Int, dataBean: DynamicBean, holder: BaseViewHolder) {
        val dataList = mutableListOf(dataBean)
        DynamicDetailsActivity.launch(context) {
            mediaKey = dataBean.mediaKey ?: ""
            dynamicVideoList = dataList
            dynamicType = dataBean.photoType
            openRecommend = true
        }
    }

    private fun setVipLevel(imageView: ImageView, item: DynamicBean) {
        if (item.bigV || item.vipLevel > 0) {
            imageView.visibility = View.VISIBLE
            imageView.setImageResource(
                if (item.bigV) R.mipmap.icon_big_v else VipGradeImageUtil.getVipImage(
                    item.vipLevel
                )
            )
        } else {
            imageView.visibility = View.GONE
        }
    }
}
package com.cqcsy.lgsp.upper

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
import kotlinx.android.synthetic.main.layout_recyclerview.*
import org.json.JSONArray

class UpperShortFragment : RefreshDataFragment<ShortVideoBean>() {
    var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments?.getInt("userId") != null) {
            userId = arguments?.getInt("userId")!!
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emptyLargeTip.setText(R.string.no_offline_download)
        emptyLittleTip.setText(R.string.no_video_little_tip)
        recyclerView.isNestedScrollingEnabled = true
    }

    override fun addDecoration(recyclerView: RecyclerView) {
        recyclerView.addItemDecoration(XLinearBuilder(recyclerView.context).setSpacing(20f).build())
    }

    override fun getUrl(): String {
        return RequestUrls.UPPER_SHORT_VIDEO_INFO
    }

    override fun getParams(): HttpParams {
        val params = HttpParams()
        params.put("userId", userId)
        return params
    }

    override fun getItemLayout(): Int {
        return R.layout.layout_upper_short_video_item
    }

    override fun parsingData(jsonArray: JSONArray): MutableList<ShortVideoBean> {
        return Gson().fromJson<ArrayList<ShortVideoBean>>(jsonArray.toString(), object :
            TypeToken<ArrayList<ShortVideoBean>>() {}.type)
    }

    override fun setItemView(holder: BaseViewHolder, item: ShortVideoBean, position: Int) {
        item.coverImgUrl?.let {
            ImageUtil.loadImage(
                this,
                it, holder.getView(R.id.image_short_video)
            )
        }
        holder.setText(R.id.short_video_time, item.duration)
        holder.setText(R.id.short_video_name, item.title)
        holder.setText(R.id.play_count, NormalUtil.formatPlayCount(item.playCount))
        holder.setText(
            R.id.update_time,
            StringUtils.getString(
                R.string.publish_time,
                TimeUtils.date2String(TimesUtils.formatDate(item.date), "yyyy-MM-dd")
            )
        )
    }

    override fun onItemClick(position: Int, dataBean: ShortVideoBean, holder: BaseViewHolder) {
        val intent = Intent(context, VideoPlayVerticalActivity::class.java)
        intent.putExtra(VideoBaseActivity.PLAY_VIDEO_BEAN, dataBean)
        startActivity(intent)
    }

    override fun isEnableClickLoading(): Boolean {
        return false
    }
}
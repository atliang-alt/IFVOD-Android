package com.cqcsy.lgsp.upper

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.MovieModuleBean
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.lgsp.video.VideoBaseActivity
import com.cqcsy.lgsp.video.VideoPlayVerticalActivity
import com.cqcsy.library.base.refresh.RefreshDataFragment
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.ImageUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.littlejerk.rvdivider.builder.XLinearBuilder
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_recyclerview.*
import org.json.JSONArray

class UpperEpisodeFragment : RefreshDataFragment<MovieModuleBean>() {
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
        return RequestUrls.UPPER_VIDEO_INFO
    }

    override fun getParams(): HttpParams {
        val params = HttpParams()
        params.put("userId", userId)
        return params
    }

    override fun getItemLayout(): Int {
        return R.layout.layout_record_film_item
    }

    override fun parsingData(jsonArray: JSONArray): MutableList<MovieModuleBean> {
        return Gson().fromJson<ArrayList<MovieModuleBean>>(jsonArray.toString(), object :
            TypeToken<ArrayList<MovieModuleBean>>() {}.type)
    }

    override fun setItemView(holder: BaseViewHolder, item: MovieModuleBean, position: Int) {
        item.coverImgUrl?.let { ImageUtil.loadImage(this, it, holder.getView(R.id.image_film)) }
        holder.setText(R.id.film_name, item.title)
        if (item.videoType != Constant.VIDEO_MOVIE) {
            holder.setText(R.id.film_episode, item.updateStatus)
            holder.setGone(R.id.film_episode, false)
        } else {
            holder.setGone(R.id.film_episode, true)
        }

        if (item.updateCount > 0) {
            holder.setVisible(R.id.item_update, true)
            if (item.videoType == Constant.VIDEO_MOVIE) {
                holder.setText(R.id.item_update, StringUtils.getString(R.string.newTips))
            } else {
                holder.setText(R.id.item_update, item.updateCount.toString())
            }
        } else {
            holder.setVisible(R.id.item_update, false)
        }

        val watchTimes = holder.getView<TextView>(R.id.content_type)
        watchTimes.text = NormalUtil.formatPlayCount(item.playCount)
        watchTimes.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_play_count, 0, 0, 0)
        holder.setText(
            R.id.film_watch_time,
            StringUtils.getString(
                R.string.publish_time,
                TimeUtils.date2String(TimesUtils.formatDate(item.publishTime), "yyyy-MM-dd")
            )
        )
    }

    override fun onItemClick(position: Int, dataBean: MovieModuleBean, holder: BaseViewHolder) {
        val intent = Intent(context, VideoPlayVerticalActivity::class.java)
        intent.putExtra(VideoBaseActivity.PLAY_VIDEO_BEAN, dataBean)
        startActivity(intent)
    }

    override fun isEnableClickLoading(): Boolean {
        return false
    }

}
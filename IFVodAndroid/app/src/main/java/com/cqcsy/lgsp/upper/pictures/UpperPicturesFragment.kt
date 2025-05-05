package com.cqcsy.lgsp.upper.pictures

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.GsonUtils
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.refresh.RefreshDataFragment
import com.cqcsy.lgsp.event.CommentEvent
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.event.VideoActionResultEvent
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.library.utils.ImageUtil
import com.google.gson.reflect.TypeToken
import com.littlejerk.rvdivider.builder.XGridBuilder
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_recyclerview.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray

/**
 * up主相册列表
 */
class UpperPicturesFragment : RefreshDataFragment<PicturesBean>() {
    companion object {
        const val PICTURES_ITEM = "picturesItem"
        const val PICTURES_PID = "picturesPid"
        const val PICTURES_TITLE = "picturesTitle"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.isNestedScrollingEnabled = true
        emptyLargeTip.setText(R.string.empty_tip)
    }

    override fun getLayoutManager(): RecyclerView.LayoutManager {
        return GridLayoutManager(context, 2)
    }

    override fun addDecoration(recyclerView: RecyclerView) {
        recyclerView.addItemDecoration(
            XGridBuilder(context).setVLineSpacing(12f).setHLineSpacing(12f).setIncludeEdge(true)
                .build()
        )
    }

    override fun getParams(): HttpParams {
        val params = HttpParams()
        params.put("userId", arguments?.getInt("userId", 0) ?: 0)
        return params
    }

    override fun getUrl(): String {
        return RequestUrls.UPPER_PICTURES
    }

    override fun getItemLayout(): Int {
        return R.layout.layout_picture_item
    }

    override fun parsingData(jsonArray: JSONArray): MutableList<PicturesBean> {
        return GsonUtils.fromJson(
            jsonArray.toString(),
            object : TypeToken<MutableList<PicturesBean>>() {}.type
        )
    }

    override fun setItemView(holder: BaseViewHolder, item: PicturesBean, position: Int) {
        holder.setText(R.id.picture_name, item.title)
        holder.setText(R.id.lookCount, NormalUtil.formatPlayCount(item.viewCount))
        holder.setText(R.id.zanCount, NormalUtil.formatPlayCount(item.likeCount))
        holder.setText(R.id.commentCount, NormalUtil.formatPlayCount(item.comments))
        holder.setText(R.id.picture_size, item.photoCount.toString())
        ImageUtil.loadImage(
            this,
            item.coverPath,
            holder.getView(R.id.picture_cover),
            defaultImage = R.mipmap.pictures_cover_default
        )
    }

    override fun isEnableClickLoading(): Boolean {
        return false
    }

    override fun onItemClick(position: Int, dataBean: PicturesBean, holder: BaseViewHolder) {
        dataBean.viewCount++
        mAdapter?.notifyItemChanged(position)
        val intent = Intent(context, PictureListActivity::class.java)
        intent.putExtra(PICTURES_PID, dataBean.mediaKey)
        intent.putExtra(PICTURES_TITLE, dataBean.title)
        startActivity(intent)
    }

    fun refreshItem(event: VideoActionResultEvent) {
        if (event.type != 2 || event.isCommentLike) {
            return
        }
        for ((index, data) in getDataList().withIndex()) {
            if (event.id == data.mediaKey) {
                data.likeCount = event.count
                data.like = event.selected
                mAdapter?.notifyItemChanged(index)
                break
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommentChange(event: CommentEvent) {
        for ((index, data) in getDataList().withIndex()) {
            if (event.mediaKey == data.mediaKey) {
                data.comments++
                mAdapter?.notifyItemChanged(index)
                break
            }
        }
    }
}
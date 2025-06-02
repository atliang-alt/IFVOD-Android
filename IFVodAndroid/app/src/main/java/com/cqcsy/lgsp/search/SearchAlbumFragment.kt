package com.cqcsy.lgsp.search

import android.content.Intent
import android.view.View
import android.widget.ImageView
import com.blankj.utilcode.util.*
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.refresh.RefreshDataFragment
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.upper.UpperActivity
import com.cqcsy.lgsp.upper.pictures.PictureListActivity
import com.cqcsy.lgsp.upper.pictures.PicturesBean
import com.cqcsy.lgsp.upper.pictures.UpperPicturesFragment
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.lgsp.vip.util.VipGradeImageUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import org.json.JSONArray

/**
 * 搜索相册页面
 */
class SearchAlbumFragment : RefreshDataFragment<PicturesBean>() {
    private var keyword = ""

    override fun getUrl(): String {
        return RequestUrls.SEARCH_ALBUM
    }

    override fun getParams(): HttpParams {
        keyword = arguments?.getString("keyword") ?: ""
        emptyLargeTip.text = StringUtils.getString(R.string.noSearchAlbum, "\"" + keyword + "\"")
        emptyLittleTip.text = ""
        val params = HttpParams()
        params.put("title", keyword)
        return params
    }

    override fun getItemLayout(): Int {
        return R.layout.item_search_album
    }

    override fun parsingData(jsonArray: JSONArray): MutableList<PicturesBean> {
        return Gson().fromJson(
            jsonArray.toString(),
            object : TypeToken<MutableList<PicturesBean>>() {}.type
        )
    }

    override fun setItemView(holder: BaseViewHolder, item: PicturesBean, position: Int) {
        ImageUtil.loadCircleImage(this, item.headImg, holder.getView(R.id.user_image))
        setVipLevel(holder.getView(R.id.userVip), item)
        holder.setText(R.id.user_nick_name, item.upperName)
        holder.setText(
            R.id.release_time,
            StringUtils.getString(
                R.string.release_time, TimeUtils.date2String(
                    TimesUtils.formatDate(item.createTime), "yyyy-MM-dd"
                )
            )
        )
        ImageUtil.loadImage(
            this,
            item.coverPath,
            holder.getView(R.id.picture_cover),
            defaultImage = R.mipmap.pictures_cover_default
        )
        holder.setText(R.id.picture_size, item.photoCount.toString())
        holder.setText(R.id.picture_name, item.title)
        if (item.description.isNullOrEmpty()) {
            holder.setGone(R.id.picture_des, true)
        } else {
            holder.setVisible(R.id.picture_des, true)
            holder.setText(R.id.picture_des, item.description)
        }
        holder.setText(R.id.comment_count, NormalUtil.formatPlayCount(item.comments))
        holder.setText(R.id.zan_count, NormalUtil.formatPlayCount(item.likeCount))
        holder.getView<ImageView>(R.id.user_image).setOnClickListener {
            val intent = Intent(context, UpperActivity::class.java)
            intent.putExtra(UpperActivity.UPPER_ID, item.uid)
            startActivity(intent)
        }
    }

    private fun setVipLevel(imageView: ImageView, item: PicturesBean) {
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

    override fun onItemClick(position: Int, dataBean: PicturesBean, holder: BaseViewHolder) {
//        super.onItemClick(position, dataBean, holder)
//        if (dataBean.uid == GlobalValue.userInfoBean?.id) {
//            val intent = Intent(context, AlbumDetailsActivity::class.java)
//            intent.putExtra(AlbumDetailsActivity.ALBUM_ID, dataBean.id)
//            startActivity(intent)
//        } else {
        val intent = Intent(context, PictureListActivity::class.java)
        intent.putExtra(UpperPicturesFragment.PICTURES_PID, dataBean.mediaKey)
        intent.putExtra(UpperPicturesFragment.PICTURES_TITLE, dataBean.title)
        startActivity(intent)
//        }
    }
}
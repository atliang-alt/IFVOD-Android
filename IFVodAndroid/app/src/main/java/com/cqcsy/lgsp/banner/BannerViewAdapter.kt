package com.cqcsy.lgsp.banner

import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.constant.TimeConstants
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.TimeUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.WebViewActivity
import com.cqcsy.lgsp.bean.AdvertBean
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.library.utils.JumpUtils
import com.cqcsy.lgsp.video.VideoBaseActivity
import com.cqcsy.lgsp.video.VideoPlayVerticalActivity
import com.cqcsy.library.utils.ImageUtil
import com.youth.banner.adapter.BannerAdapter

class BannerViewAdapter(data: List<AdvertBean>, val context: Context) :
    BannerAdapter<AdvertBean, BannerViewAdapter.ImageHolder>(data) {

    override fun onCreateHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        val layout = FrameLayout(parent.context)
        layout.layoutParams = params
        layout.setBackgroundResource(R.color.background_4)

        val imageView = ImageView(parent.context)
        imageView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        imageView.scaleType = ImageView.ScaleType.CENTER
        imageView.tag = "largeImage"

        val adTag = TextView(context)
        val tagParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        val size5 = SizeUtils.dp2px(5f)
        val size2 = SizeUtils.dp2px(2f)
        tagParams.topMargin = size5
        tagParams.leftMargin = size5
        adTag.setPadding(size5, size2, size5, size2)

        adTag.tag = "adTag"
        adTag.setBackgroundResource(R.drawable.background_black_corner_2)
        adTag.setText(R.string.advertisement)
        adTag.setTextColor(ColorUtils.getColor(R.color.word_color_8))
        adTag.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
        adTag.gravity = Gravity.CENTER
        adTag.layoutParams = tagParams
        adTag.visibility = View.GONE

        layout.addView(imageView)
        layout.addView(adTag)
        return ImageHolder(layout)
    }

    override fun onBindView(holder: ImageHolder, data: AdvertBean, position: Int, size: Int) {
        ImageUtil.loadImage(context, data.showURL, holder.imageView, 0, needAuthor = true)
        if (data.resourceType == 1) {   // 广告
            holder.adTag.visibility = View.VISIBLE
        } else {
            holder.adTag.visibility = View.GONE
        }
        holder.imageView.setOnClickListener {
//            if (!data.playtime.isNullOrEmpty()) {
//                val start = TimesUtils.utc2Local(data.playtime!!, "yyyy-MM-dd HH:mm:ss")
//                val diffTime = TimeUtils.getTimeSpanByNow(start, TimeConstants.MIN)
//                if (diffTime > 0) {
//                    return@setOnClickListener
//                }
//            }
            if (JumpUtils.isJumpHandle(data.appParam)) {
                JumpUtils.jumpAnyUtils(context, data.appParam!!)
            } else if (!data.linkURL.isNullOrEmpty()) {
                val intent = Intent(context, WebViewActivity::class.java)
                intent.putExtra(WebViewActivity.urlKey, data.linkURL)
                context.startActivity(intent)
            } else if (data.mediaItem != null) {
                val intent = Intent(context, VideoPlayVerticalActivity::class.java)
                intent.putExtra(VideoBaseActivity.PLAY_VIDEO_BEAN, data.mediaItem)
                context.startActivity(intent)
            }
        }
    }

    class ImageHolder(view: FrameLayout) : RecyclerView.ViewHolder(view) {
        var imageView: ImageView = view.findViewWithTag("largeImage")
        var adTag: TextView = view.findViewWithTag("adTag")
    }

}
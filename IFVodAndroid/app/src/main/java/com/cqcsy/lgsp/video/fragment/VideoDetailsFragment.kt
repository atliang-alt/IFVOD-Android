package com.cqcsy.lgsp.video.fragment

import android.text.Html
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalFragment
import com.cqcsy.lgsp.bean.VideoDetailsBean
import com.cqcsy.lgsp.bean.VideoRatingBean
import com.cqcsy.library.utils.Constant
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.library.utils.ImageUtil
import kotlinx.android.synthetic.main.layout_video_detail_fragment.*

/**
 * 视频详情页dialog
 */
class VideoDetailsFragment : NormalFragment() {
    private var videoDetailsBean: VideoDetailsBean? = null
    private var mRating: VideoRatingBean? = null

    override fun getContainerView(): Int {
        return R.layout.layout_video_detail_fragment
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        return if (enter) {
            AnimationUtils.loadAnimation(requireActivity(), R.anim.bottom_in)
        } else {
            AnimationUtils.loadAnimation(requireActivity(), R.anim.bottom_out)
        }
    }

    override fun initData() {
        super.initData()
        videoDetailsBean = arguments?.getSerializable("videoDetailsBean") as VideoDetailsBean
        mRating = arguments?.getSerializable("videoRating") as VideoRatingBean?
        close.setOnClickListener {
            parentFragmentManager.beginTransaction().remove(this).commitNowAllowingStateLoss()
        }
    }

    override fun initView() {
        super.initView()
        if (videoDetailsBean?.videoType == Constant.VIDEO_SHORT) {
            videoDetailYear.visibility = View.GONE
            videoDetailType.visibility = View.GONE
            scoreLine.visibility = View.GONE
            videoDetailDirector.visibility = View.GONE
            videoDetailActor.visibility = View.GONE
            videoDetailClassify.text = videoDetailsBean?.contentType
        } else {
            videoDetailYear.visibility = View.VISIBLE
            scoreLine.visibility = View.VISIBLE
            videoDetailYear.text = TimesUtils.getYear(videoDetailsBean?.postTime ?: "")
            if (!mRating?.ratingList.isNullOrEmpty()) {
                mRating?.ratingList?.forEach {
                    val item = LayoutInflater.from(context).inflate(R.layout.layout_rating, null)
                    val logo = item.findViewById<ImageView>(R.id.rating_logo)
                    val score = item.findViewById<TextView>(R.id.score)
                    ImageUtil.loadImage(this, it?.logo, logo)
                    score.text = it?.rating.toString()
                    rating_container.addView(item)
                }
            } else {
                val textView = TextView(context)
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                textView.setTextColor(ColorUtils.getColor(R.color.word_color_5))
                textView.text = mRating?.rating
                textView.setPadding(SizeUtils.dp2px(10f), 0, 0, 0)
                rating_container.addView(textView)
            }
            videoDetailDirector.visibility = View.VISIBLE
            videoDetailDirector.text = getString(
                R.string.director_detail,
                if (videoDetailsBean?.director.isNullOrEmpty()) {
                    StringUtils.getString(R.string.unknown)
                } else {
                    videoDetailsBean?.director
                }
            )
            videoDetailActor.visibility = View.VISIBLE
            videoDetailActor.text = getString(
                R.string.actor_detail,
                if (videoDetailsBean?.director.isNullOrEmpty()) {
                    StringUtils.getString(R.string.unknown)
                } else {
                    NormalUtil.formatActorName(
                        videoDetailsBean?.actor ?: StringUtils.getString(R.string.unknown)
                    )
                }
            )
            videoDetailType.visibility = View.VISIBLE
            videoDetailType.text = videoDetailsBean?.typeName

            val stringBuffer = StringBuffer()
            if (!videoDetailsBean?.cidMapper.isNullOrEmpty()) {
                stringBuffer.append(videoDetailsBean?.cidMapper?.replace(",", "·"))
            }
            if (!videoDetailsBean?.regional.isNullOrEmpty()) {
                stringBuffer.append("·" + videoDetailsBean?.regional)
            }
            if (!videoDetailsBean?.lang.isNullOrEmpty()) {
                stringBuffer.append("·" + videoDetailsBean?.lang)
            }
            videoDetailClassify.text = stringBuffer

            if (videoDetailsBean?.videoType == Constant.VIDEO_MOVIE) {
                videoUpdateInfo.visibility = View.GONE
            } else {
                videoUpdateInfo.visibility = View.VISIBLE
                val sb = StringBuffer()
                sb.append(videoDetailsBean?.updateStatus)
                if (!videoDetailsBean?.updateMsg.isNullOrEmpty()) {
                    sb.append(" ")
                    sb.append(StringUtils.getString(R.string.each, videoDetailsBean?.updateMsg))
                }
                videoUpdateInfo.text = sb
            }
        }
        videoTitle.text = videoDetailsBean?.title
        videoDetailPlayCount.text = NormalUtil.formatPlayCount(videoDetailsBean?.playCount ?: 0)
        videoDetailIntroduction.text = Html.fromHtml(videoDetailsBean?.introduce)
    }
}
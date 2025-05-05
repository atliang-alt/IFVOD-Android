package com.cqcsy.lgsp.upload

import android.content.Context
import android.util.AttributeSet
import com.cqcsy.lgsp.R
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer

/**
 * 作者：wangjianxiong
 * 创建时间：2023/3/28
 *
 *
 */
class PreviewVideoPlayer(context: Context, attrs: AttributeSet) :
    StandardGSYVideoPlayer(context, attrs) {
    override fun getLayoutId(): Int {
        return R.layout.layout_preview_video_player
    }
}
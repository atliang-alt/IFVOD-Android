package com.cqcsy.lgsp.upper.pictures

import android.os.Bundle
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.video.VideoBaseActivity
import com.cqcsy.lgsp.video.fragment.VideoCommentFragment
import com.cqcsy.library.base.NormalActivity

/**
 * 相册评论列表
 */
class PicturesCommentListActivity : NormalActivity() {
    companion object {
        const val PICTURES_MEDIA_ID = "picturesMediaId"
        const val PICTURES_COMMENT = "picturesComments"
        const val PICTURES_TITLE = "picturesTitle"
        const val PICTURES_TYPE = "picturesType"
    }

    override fun getContainerView(): Int {
        return R.layout.activity_news_second
    }

    override fun onViewCreate() {
        val showInput = intent.getBooleanExtra(VideoCommentFragment.SHOW_INPUT, true)
        val title = intent.getStringExtra(PICTURES_TITLE) ?: ""
        val mediaKey = intent.getStringExtra(PICTURES_MEDIA_ID) ?: ""
        val commentCounts = intent.getIntExtra(PICTURES_COMMENT, 0)
        val videoType = intent.getIntExtra(PICTURES_TYPE, 0)
        val commentId = intent.getIntExtra(VideoBaseActivity.COMMENT_ID, 0)
        val replyId = intent.getIntExtra(VideoBaseActivity.REPLY_ID, 0)
        setHeaderTitle(title)
        val bundle = Bundle()
        bundle.putString("mediaKey", mediaKey)
        bundle.putInt("commentCount", commentCounts)
        bundle.putBoolean("isShowHeader", true)
        bundle.putInt("videoType", videoType)
        bundle.putInt(VideoCommentFragment.FORM_TYPE, VideoCommentFragment.ALBUM_TYPE)
        bundle.putBoolean(VideoCommentFragment.SHOW_INPUT, showInput)
        bundle.putInt(VideoBaseActivity.COMMENT_ID, commentId)
        bundle.putInt(VideoBaseActivity.REPLY_ID, replyId)
        val fragment = VideoCommentFragment()
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction().add(R.id.container, fragment).commitAllowingStateLoss()
    }

}
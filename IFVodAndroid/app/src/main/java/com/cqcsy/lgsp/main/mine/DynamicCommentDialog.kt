package com.cqcsy.lgsp.main.mine

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import com.blankj.utilcode.util.ScreenUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.video.VideoBaseActivity
import com.cqcsy.lgsp.video.fragment.VideoCommentFragment
import kotlinx.android.synthetic.main.layout_dynamic_comment.*

/**
 * 作者：wangjianxiong
 * 创建时间：2022/8/22
 *
 *
 */
class DynamicCommentDialog : DialogFragment() {

    companion object {
        private const val TAG = "DynamicCommentDialog"

        @JvmStatic
        fun show(
            fragmentManager: FragmentManager,
            commentId: Int,
            replyId: Int,
            mediaKey: String,
            commentCount: Int,
            showInput: Boolean,
            videoType: Int?
        ) {
            if (!fragmentManager.isDestroyed) {
                val dialogFragment = DynamicCommentDialog()
                dialogFragment.arguments = Bundle().apply {
                    putBoolean("showInput", showInput)
                    putString("mediaKey", mediaKey)
                    putInt("videoType", videoType ?: 0)
                    putInt("formType", VideoCommentFragment.DYNAMIC_TYPE)
                    putInt("commentCount", commentCount)
                    putInt(VideoBaseActivity.COMMENT_ID, commentId)
                    putInt(VideoBaseActivity.REPLY_ID, replyId)
                }
                dialogFragment.show(fragmentManager, TAG)
            }
        }
    }

    private var mediaKey: String = ""
    private var commentCount: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_dynamic_comment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mediaKey = arguments?.getString("mediaKey") ?: ""
        commentCount = arguments?.getInt("commentCount") ?: 0
        setCommentCount(commentCount)
        childFragmentManager.commit {
            val fragment = VideoCommentFragment()
            fragment.arguments = arguments
            replace(R.id.fragment_container, fragment, "comment_fragment")
            setMaxLifecycle(fragment, Lifecycle.State.RESUMED)
        }
        iv_close.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            val params: WindowManager.LayoutParams = attributes
            setBackgroundDrawableResource(android.R.color.transparent)
            params.windowAnimations = R.style.bottom_dialog_anim
            params.gravity = Gravity.BOTTOM
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.dimAmount = 0f
            params.height = ScreenUtils.getScreenHeight() * 2 / 3
            this.attributes = params
        }
    }

    private fun setCommentCount(count: Int) {
        tv_comment_count.text = context?.getString(R.string.albumCommentCount, count)
    }
}
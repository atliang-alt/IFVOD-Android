package com.cqcsy.lgsp.utils

import android.content.Context
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.StringUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.CommentBean
import com.cqcsy.lgsp.bean.VoteOptionBean

/**
 * 投票结果Item处理类
 */
object VoteResultUtil {
    private val progressBarBg = arrayOf(
        R.color.progressBar_1,
        R.color.progressBar_2,
        R.color.progressBar_3,
        R.color.progressBar_4,
        R.color.progressBar_5
    )

    private val optionNumb = arrayOf("A.", "B.", "C.", "D.", "E.")

    /**
     * 添加投票结果View
     * list 投票选项
     * isVote 是否已投票
     * allVote 参与投票总人数
     */
    fun addVoteResultView(
        context: Context,
        commentBean: CommentBean,
        linearLayout: LinearLayout
    ) {
        linearLayout.removeAllViews()
        val list = commentBean.voteItem
        val allVoteCount = getAllVoteCount(list)
        for (i in list.indices) {
            if (i >= 5) {
                return
            }
            val view =
                LayoutInflater.from(context).inflate(R.layout.item_vote_option, linearLayout, false)
            val textNumb = view.findViewById(R.id.optionNumb) as TextView
            val textName = view.findViewById(R.id.optionName) as TextView
            val textCount = view.findViewById(R.id.optionCount) as TextView
            val progressBar = view.findViewById(R.id.optionProgressBar) as ProgressBar
            textNumb.text = optionNumb[i]
            textName.text = list[i].option
            textCount.text = StringUtils.getString(R.string.optionVoteCount, list[i].count)
            if (commentBean.voteStatus) {
                progressBar.visibility = View.VISIBLE
                textCount.visibility = View.VISIBLE
            } else {
                progressBar.visibility = View.GONE
                textCount.visibility = View.GONE
            }
            setProgress(i, progressBar, false)
            //设置进度值
            progressBar.progress = (list[i].count / allVoteCount.toFloat() * 100).toInt()
            linearLayout.addView(view)
        }
    }

    /**
     * 添加投票Item，适用于 我参与的/我发起的
     */
    fun mineVoteResultView(
        context: Context,
        voteOptionList: MutableList<VoteOptionBean>,
        linearLayout: LinearLayout,
        isClose: Boolean
    ) {
        linearLayout.removeAllViews()
        val allVoteCount = getAllVoteCount(voteOptionList)
        for (i in voteOptionList.indices) {
            if (i >= 5) {
                return
            }
            val view =
                LayoutInflater.from(context).inflate(R.layout.item_vote_option, linearLayout, false)
            val textNumb = view.findViewById(R.id.optionNumb) as TextView
            val textName = view.findViewById(R.id.optionName) as TextView
            val textCount = view.findViewById(R.id.optionCount) as TextView
            val progressBar = view.findViewById(R.id.optionProgressBar) as ProgressBar
            textNumb.text = optionNumb[i]
            textName.text = voteOptionList[i].option
            textCount.text =
                StringUtils.getString(R.string.optionVoteCount, voteOptionList[i].count)
            progressBar.visibility = View.VISIBLE
            textCount.visibility = View.VISIBLE
            setProgress(i, progressBar, isClose)
            //设置进度值
            progressBar.progress = (voteOptionList[i].count / allVoteCount.toFloat() * 100).toInt()
            linearLayout.addView(view)
        }
    }

    /**
     * 获取总票数
     */
    fun getAllVoteCount(list: MutableList<VoteOptionBean>): Int {
        var count = 0
        for (i in list.indices) {
            count += list[i].count
        }
        return count
    }

    /**
     * 动态设置progress进度条颜色
     */
    private fun setProgress(position: Int, progressBar: ProgressBar, isClose: Boolean) {
        //准备progressBar带圆角的进度条Drawable
        val progressContent = GradientDrawable()
        progressContent.cornerRadius = 0f
        //设置绘制颜色，此处可以自己获取不同的颜色
        if (isClose) {
            progressContent.setColor(ColorUtils.getColor(R.color.background_progress_bar))
        } else {
            progressContent.setColor(ColorUtils.getColor(getColorForIndex(position)))
        }
        //ClipDrawable是对一个Drawable进行剪切操作，可以控制这个drawable的剪切区域，以及相相对于容器的对齐方式
        val progressClip =
            ClipDrawable(progressContent, Gravity.START, ClipDrawable.HORIZONTAL)
        //设置progressBarDrawable
        progressBar.progressDrawable = progressClip
    }

    /**
     * 获取progress进度条颜色
     */
    private fun getColorForIndex(position: Int): Int {
        return progressBarBg[position]
    }
}
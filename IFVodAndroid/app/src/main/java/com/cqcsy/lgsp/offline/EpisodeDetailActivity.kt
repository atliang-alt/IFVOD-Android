package com.cqcsy.lgsp.offline

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.ColorUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.lgsp.bean.VideoBaseBean
import com.cqcsy.lgsp.download.DownloadMgr
import com.cqcsy.lgsp.video.AnthologyActivity
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.video.VideoBaseActivity
import com.cqcsy.lgsp.video.VideoPlayVerticalActivity
import com.cqcsy.library.views.TipsDialog
import com.google.gson.Gson
import com.littlejerk.rvdivider.builder.XGridBuilder
import com.lzy.okgo.db.DownloadManager
import com.lzy.okgo.utils.IOUtils
import kotlinx.android.synthetic.main.activity_episode_detail.*
import java.io.File
import java.io.Serializable

/**
 * 剧集、综艺已下载管理页
 */

class EpisodeDetailActivity : NormalActivity() {
    var isEdit = false
    var bean: VideoBaseBean? = null
    val selected: MutableList<VideoBaseBean> = ArrayList()

    override fun getContainerView(): Int {
        return R.layout.activity_episode_detail
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data = intent.getSerializableExtra(VideoBaseActivity.PLAY_VIDEO_BEAN)
        if (data != null && data is VideoBaseBean) {
            bean = data
            data.title?.let { setHeaderTitle(it) }
            setRightText(R.string.edit)
            userStorage.text = getString(R.string.user_storage, NormalUtil.formatFileSize(this, data.size))
            initList()
        } else {
            downloadAction.isEnabled = false
        }
    }

    private fun getLocalDownload(bean: VideoBaseBean): MutableList<VideoBaseBean> {
        val folderName = File(bean.filePath).parent + File.separator
        val data = ArrayList<VideoBaseBean>()
        val finished = DownloadMgr.getDownloadFinished()
        loop@ for (progress in finished) {
            if (folderName != progress.folder || progress.extra1 == null || progress.extra1.toString()
                    .isEmpty() || !File(progress.filePath).exists()
            ) {
                continue@loop
            }
            val temp = Gson().fromJson(progress.extra1.toString(), VideoBaseBean::class.java)
            temp.size = progress.totalSize
            temp.filePath = progress.filePath
            data.add(temp)
        }
        return data
    }

    private fun initList() {
        val data = getLocalDownload(bean!!)
        val row = if ((data.filter { (it.episodeTitle?.length ?: 0) >= 5 }).isNullOrEmpty()) {
            5
        } else {
            2
        }
        detailList.layoutManager = GridLayoutManager(this, row)
        detailList.addItemDecoration(XGridBuilder(this).setHLineSpacing(10f).setVLineSpacing(15f).setIncludeEdge(true).build())
        setDetailList(data)
    }

    private fun setDetailList(data: MutableList<VideoBaseBean>) {
        if (data.isEmpty()) {
            finish()
            return
        }
        data.sortBy { it.episodeTitle }
        val adapter = object : BaseQuickAdapter<VideoBaseBean, BaseViewHolder>(R.layout.layout_name_item, data) {
            override fun convert(holder: BaseViewHolder, item: VideoBaseBean) {
                holder.setText(R.id.item_name, item.episodeTitle)
                if (item.isWatched) {
                    holder.setTextColor(R.id.item_name, ColorUtils.getColor(R.color.grey_2))
                } else {
                    holder.setTextColor(R.id.item_name, ColorUtils.getColor(R.color.word_color_12))
                }
                val check = holder.getView<CheckBox>(R.id.item_check)
                if (isEdit) {
                    check.visibility = View.VISIBLE
                    check.isChecked = selected.contains(item)
                    check.setOnClickListener {
                        if (selected.contains(item)) {
                            selected.remove(item)
                        } else {
                            selected.add(item)
                        }
                        notifyItemChanged(holder.adapterPosition)
                    }
                } else {
                    check.visibility = View.GONE
                }
            }

        }
        adapter.setOnItemClickListener { adapter, view, position ->
            val bean = adapter.getItem(position) as VideoBaseBean
            if (isEdit) {
                if (selected.contains(bean)) {
                    selected.remove(bean)
                } else {
                    selected.add(bean)
                }
            } else {
                bean.isWatched = true
                val progress = DownloadManager.getInstance().get(bean.episodeKey)
                if (progress?.extra1 == null) {
                    return@setOnItemClickListener
                }
                val intent = Intent(this, VideoPlayVerticalActivity::class.java)
                val temp = Gson().fromJson(progress.extra1.toString(), VideoBaseBean::class.java)
                temp.size = progress.totalSize
                temp.filePath = progress.filePath
                intent.putExtra(VideoBaseActivity.PLAY_VIDEO_BEAN, temp)
                intent.putExtra(VideoBaseActivity.DOWNLOADED_EPISODE, data as Serializable)
                startActivity(intent)
            }
            adapter.notifyItemChanged(position)
        }
        detailList.adapter = adapter
    }

    private fun resetList() {
        onRightClick(rightContent)
        if (bean != null && detailList.adapter is BaseQuickAdapter<*, *>) {
            (detailList.adapter as BaseQuickAdapter<VideoBaseBean, BaseViewHolder>).setList(getLocalDownload(bean!!))
        }
    }

    override fun onRightClick(view: View) {
        if (isEdit) {
            setRightText(R.string.edit)
            editContent.visibility = View.GONE
            leftImageView.visibility = View.VISIBLE
            downloadAction.isEnabled = true
        } else {
            setRightText(R.string.cancel)
            editContent.visibility = View.VISIBLE
            leftImageView.visibility = View.INVISIBLE
            downloadAction.isEnabled = false
        }
        isEdit = !isEdit
        selected.clear()
        detailList.adapter?.notifyDataSetChanged()
    }

    fun downloadMoreClick(view: View) {
        val intent = Intent(this, AnthologyActivity::class.java)
        intent.putExtra("mediaKey", bean?.mediaKey)
        intent.putExtra("videoType", bean?.videoType)
        intent.putExtra("pageAction", 1)
        startActivity(intent)
    }

    fun clearAll(view: View) {
        val tipsDialog = TipsDialog(this)
        tipsDialog.setDialogTitle(R.string.clear_all)
        tipsDialog.setMsg(R.string.clear_tips)
        tipsDialog.setLeftListener(R.string.think_again) {
            tipsDialog.dismiss()
        }
        tipsDialog.setRightListener(R.string.clear) {
            tipsDialog.dismiss()
            delete(true)
            resetList()
        }
        tipsDialog.show()
    }

    fun delete(view: View) {
        val tipsDialog = TipsDialog(this)
        tipsDialog.setDialogTitle(R.string.delete_record)
        tipsDialog.setMsg(R.string.delete_video_tips)
        tipsDialog.setLeftListener(R.string.save) {
            tipsDialog.dismiss()
        }
        tipsDialog.setRightListener(R.string.delete) {
            tipsDialog.dismiss()
            delete(false)
            resetList()
        }
        tipsDialog.show()
    }

    private fun delete(isClear: Boolean) {
        setResult(Activity.RESULT_OK)
        if (isClear) {
            val folder = File(bean?.filePath).parent + File.separator
            val list = DownloadMgr.getDownloadFinished()
            for (progress in list) {
                if (progress.folder == folder) {
                    DownloadMgr.deleteTask(progress.tag, true)
                }
            }
            IOUtils.delFileOrFolder(folder)
            finish()
        } else {
            for (bean in selected) {
                bean.episodeKey.let { DownloadMgr.deleteTask(it, true) }
            }
            setDetailList(getLocalDownload(bean!!))
        }
    }
}
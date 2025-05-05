package com.cqcsy.lgsp.offline

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.CheckBox
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.refresh.RefreshFragment
import com.cqcsy.lgsp.bean.VideoBaseBean
import com.cqcsy.lgsp.download.DownloadMgr
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.video.VideoBaseActivity
import com.cqcsy.lgsp.video.VideoPlayVerticalActivity
import com.google.gson.Gson
import com.littlejerk.rvdivider.builder.XLinearBuilder
import com.lzy.okgo.model.Progress
import kotlinx.android.synthetic.main.layout_recyclerview.*
import java.io.File

/**
 * 剧集、电影、综艺已下载列表
 */
class EpisodeOfflineFragment : RefreshFragment() {
    var selectedItem: MutableList<VideoBaseBean> = ArrayList()

    var isEdit = false

    fun getCount(): Int {
        return if (recyclerView.adapter == null) 0 else (recyclerView.adapter as BaseQuickAdapter<*, *>).data.size
    }

    override fun getRefreshChild(): Int {
        return R.layout.layout_recyclerview
    }

    override fun initData() {
        super.initData()
        emptyLargeTip.setText(R.string.no_offline_download)
        emptyLittleTip.setText(R.string.no_offline_download_tip)
        disableRefresh()
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    override fun onResume() {
        super.onResume()

        setListData()
    }

    override fun onVisible() {
        super.onVisible()
        setListData()
    }

    private fun getRecord(): MutableList<VideoBaseBean> {
        val data = ArrayList<VideoBaseBean>()
        val finished = DownloadMgr.getDownloadFinished()
        val folderList = ArrayList<String>()
        loop@ for (progress in finished) {
            if (progress.extra1 == null || progress.extra1.toString()
                    .isEmpty() || !File(progress.filePath).exists()
            ) {
                continue@loop
            }
            val bean = Gson().fromJson(
                progress.extra1.toString(),
                VideoBaseBean::class.java
            )
            bean.size = progress.totalSize
            bean.filePath = progress.filePath
            when (bean.videoType) {
                Constant.VIDEO_MOVIE -> data.add(bean)
                Constant.VIDEO_TELEPLAY, Constant.VIDEO_VARIETY -> {
                    if (folderList.contains(progress.folder)) {
                        continue@loop
                    }
                    folderList.add(progress.folder)
                    data.add(getRecordFolderAll(finished, progress.folder))
                }
            }
        }
        return data
    }

    private fun getRecordFolderAll(
        data: MutableList<Progress>,
        folderName: String
    ): VideoBaseBean {
        val bean = VideoBaseBean()
        for (progress in data) {
            if (progress.folder.contains(folderName) && progress.extra1 != null && progress.extra1.toString()
                    .isNotEmpty() && File(
                    progress.filePath
                ).exists()
            ) {
                val temp = Gson().fromJson<VideoBaseBean>(
                    progress.extra1.toString(),
                    VideoBaseBean::class.java
                )
                bean.videoNumber++
                bean.videoType = temp.videoType
                bean.title = temp.title
                bean.mediaKey = temp.mediaKey
                bean.upperName = temp.upperName
                bean.coverImgUrl = temp.coverImgUrl
                bean.mediaUrl = temp.mediaUrl
                bean.size += progress.totalSize
                bean.filePath = progress.filePath
            }
        }
        return bean
    }

    fun refreshList(isEdit: Boolean) {
        this.isEdit = isEdit
        selectedItem.clear()
        setListData()
    }

    private fun setListData() {
        val data = getRecord()
        if (data.isEmpty()) {
            showEmpty()
        } else {
            if (recyclerView.adapter == null) {
                setRecord(data)
            } else {
                (recyclerView.adapter as BaseQuickAdapter<VideoBaseBean, BaseViewHolder>).setList(
                    data
                )
            }
        }
        (activity as OfflineActivity).setRightState(this)
    }

    fun getSelectItem(): MutableList<VideoBaseBean> {
        val result: MutableList<VideoBaseBean> = ArrayList()
        val finished = getAllVideoByType(
            Constant.VIDEO_TELEPLAY,
            Constant.VIDEO_VARIETY
        )
        selectedItem.forEach {
            if (it.videoType == Constant.VIDEO_MOVIE) {
                result.add(it)
            } else {
                for (value in finished) {
                    if (value.mediaKey == it.mediaKey) {
                        result.add(value)
                    }
                }
            }
        }
        return result
    }

    fun getAllDownloaded(): MutableList<VideoBaseBean> {
        return getAllVideoByType(
            Constant.VIDEO_MOVIE,
            Constant.VIDEO_TELEPLAY,
            Constant.VIDEO_VARIETY
        )
    }

    private fun getAllVideoByType(vararg type: Int): MutableList<VideoBaseBean> {
        val finished = DownloadMgr.getDownloadFinished()
        val result: MutableList<VideoBaseBean> = ArrayList()
        loop@ for (progress in finished) {
            if (progress.extra1 == null || progress.extra1.toString()
                    .isEmpty() || !File(progress.filePath).exists()
            ) {
                continue@loop
            }
            val bean = Gson().fromJson<VideoBaseBean>(
                progress.extra1.toString(),
                VideoBaseBean::class.java
            )
            if (bean.videoType in type) {
                bean.size = progress.totalSize
                bean.filePath = progress.filePath
                result.add(bean)
            }
        }
        return result
    }

    fun removeItems(removeData: MutableList<VideoBaseBean>) {
        (recyclerView.adapter as BaseQuickAdapter<VideoBaseBean, BaseViewHolder>).data.removeAll(
            removeData
        )
        (activity as OfflineActivity).setRightState(this)
    }

    private fun showList() {
        dismissProgress()
        recyclerView.visibility = View.VISIBLE
    }

    private fun setRecord(data: MutableList<VideoBaseBean>) {
        if (data.isEmpty()) {
            showEmpty()
            (activity as OfflineActivity).setRightState(this)
            return
        }
        showList()
        val adapter: BaseQuickAdapter<VideoBaseBean, BaseViewHolder> =
            object : BaseQuickAdapter<VideoBaseBean, BaseViewHolder>(
                R.layout.layout_offline_film_item,
                data
            ) {
                override fun convert(holder: BaseViewHolder, item: VideoBaseBean) {
                    val checkBox = holder.getView<CheckBox>(R.id.item_check)
                    if (isEdit) {
                        checkBox.visibility = View.VISIBLE
                    } else {
                        checkBox.visibility = View.GONE
                    }
                    checkBox.isChecked = selectedItem.contains(item)
                    checkBox.setOnClickListener {
                        if (selectedItem.contains(item)) {
                            selectedItem.remove(item)
                        } else {
                            selectedItem.add(item)
                        }
                    }
                    item.coverImgUrl?.let {
                        ImageUtil.loadImage(
                            context,
                            it, holder.getView(R.id.image_film)
                        )
                    }
                    holder.setText(R.id.film_name, item.title)
                    if (item.videoType == 0) {
                        holder.setText(R.id.film_total, "")
                    } else {
                        holder.setText(
                            R.id.film_total,
                            getString(R.string.total_video, item.videoNumber)
                        )
                    }
                    holder.setText(R.id.film_size, NormalUtil.formatFileSize(context, item.size))
                }

            }
        adapter.setOnItemClickListener { adapter, view, position ->
            val bean = adapter.getItem(position) as VideoBaseBean
            if (!isEdit) {
                startVideoPlay(bean)
            } else {
                if (selectedItem.contains(bean)) {
                    selectedItem.remove(bean)
                } else {
                    selectedItem.add(bean)
                }
                view.findViewById<CheckBox>(R.id.item_check).isChecked = selectedItem.contains(bean)
            }
        }
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            XLinearBuilder(requireContext()).setSpacing(10f).build()
        )
        (activity as OfflineActivity).setRightState(this)
    }

    private fun startVideoPlay(bean: VideoBaseBean) {
        val intent: Intent
        if (bean.videoType == Constant.VIDEO_MOVIE || bean.videoType == Constant.VIDEO_SHORT) {
            // 直接播放
            intent = Intent(context, VideoPlayVerticalActivity::class.java)
            intent.putExtra(
                VideoBaseActivity.PLAY_VIDEO_BEAN, bean
            )
        } else {
            // 跳转选集
            intent = Intent(context, EpisodeDetailActivity::class.java)
            intent.putExtra(
                VideoBaseActivity.PLAY_VIDEO_BEAN, bean
            )
        }
        startActivityForResult(intent, 2000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            setListData()
        }
    }

    override fun isEnableClickLoading(): Boolean {
        return false
    }
}
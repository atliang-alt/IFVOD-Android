package com.cqcsy.lgsp.offline

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
import kotlinx.android.synthetic.main.layout_recyclerview.*
import java.io.File

/**
 * 短视频离线列表
 */
class ShortVideoOfflineFragment : RefreshFragment() {
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
        loop@ for (progress in finished) {
            if (progress.extra1 == null || progress.extra1.toString()
                    .isEmpty() || !File(progress.filePath).exists() || progress.totalSize == 0L
            ) {
                continue@loop
            }
            val bean = Gson().fromJson(
                progress.extra1.toString(),
                VideoBaseBean::class.java
            )
            bean.filePath = progress.filePath
            bean.size = progress.totalSize
            if (bean.videoType == Constant.VIDEO_SHORT) {
                data.add(bean)
            }
        }
        return data
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
        return selectedItem
    }

    fun getAllDownloaded(): MutableList<VideoBaseBean>? {
        if (recyclerView.adapter != null) {
            return (recyclerView.adapter as BaseQuickAdapter<VideoBaseBean, BaseViewHolder>).data
        }
        return null
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
                R.layout.layout_short_video_offline_item,
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
                            it,
                            holder.getView(R.id.image_short_video),
                            0
                        )
                    }
                    holder.setText(R.id.short_video_time, item.duration)
                    holder.setText(R.id.short_video_name, item.title)
                    holder.setText(R.id.short_video_owner, item.upperName)
                    holder.setText(R.id.video_size, NormalUtil.formatFileSize(context, item.size))
                }

            }
        adapter.setOnItemClickListener { adapter, view, position ->
            val bean = adapter.getItem(position) as VideoBaseBean
            if (!isEdit) {
                val intent = Intent(context, VideoPlayVerticalActivity::class.java)
                intent.putExtra(VideoBaseActivity.PLAY_VIDEO_BEAN, bean)
                startActivity(intent)
            } else {
                if (selectedItem.contains(bean)) {
                    selectedItem.remove(bean)
                } else {
                    selectedItem.add(bean)
                }
                view.findViewById<CheckBox>(R.id.item_check).isChecked = selectedItem.contains(bean)
//                adapter.notifyItemChanged(position)
            }
        }
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            XLinearBuilder(requireContext()).setSpacing(10f).build()
        )
        (activity as OfflineActivity).setRightState(this)
    }
    override fun isEnableClickLoading(): Boolean {
        return false
    }
}
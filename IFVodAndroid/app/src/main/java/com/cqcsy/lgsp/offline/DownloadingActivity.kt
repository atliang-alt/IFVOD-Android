package com.cqcsy.lgsp.offline

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.VideoBaseBean
import com.cqcsy.lgsp.download.DownloadMgr
import com.cqcsy.library.download.server.DownloadListener
import com.cqcsy.library.download.server.OkDownload
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.library.views.TipsDialog
import com.google.gson.Gson
import com.littlejerk.rvdivider.builder.XLinearBuilder
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Progress
import kotlinx.android.synthetic.main.activity_downloading.*
import org.json.JSONObject
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque

/**
 * 正在下载页面
 */
class DownloadingActivity : NormalActivity() {
    var isEdit = false
    var selectedItem: MutableList<Progress> = ArrayList()
    private val taskQueue: Queue<Progress> = ConcurrentLinkedDeque()
    private var currentProgress: Progress? = null
    var currentHolder: BaseViewHolder? = null
    private val mHandler: Handler = Handler(Looper.getMainLooper())

    private var isPauseAll = false

    override fun getContainerView(): Int {
        return R.layout.activity_downloading
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.downloading)
        setRightText(R.string.edit)
        initList()
        setListData()
    }

    private fun initList() {
        downloadingList.layoutManager = LinearLayoutManager(this)
        downloadingList.addItemDecoration(XLinearBuilder(this).setSpacing(10f).build())
    }

    private fun setListData() {
        taskQueue.clear()
        val downloading = DownloadMgr.getDownloading()
        downloading.forEach {
            if (it.status == Progress.LOADING) {
                currentProgress = it
            } else {
                taskQueue.add(it)
            }
        }
        if (downloading.size == 0) {
            setEmpty()
        } else {
            setRecord(downloading)
        }
    }

    private fun checkDownloading() {
        mHandler.postDelayed({
            val downloading = DownloadMgr.getDownloading()
            downloading.forEach {
                if (it.status == Progress.LOADING) {
                    currentProgress = it
                    setListener(it)
                    downloadingList.adapter?.notifyDataSetChanged()
                    return@forEach
                }
            }
            if (currentProgress == null) {
                checkDownloading()
            }
        }, 3000)
    }

    override fun onResume() {
        super.onResume()
        if (!isPauseAll && currentProgress != null) {
            setListener(currentProgress!!)
        } else {
            checkDownloading()
        }
    }

    override fun onPause() {
        super.onPause()
        mHandler.removeCallbacksAndMessages(null)
        if (currentProgress != null) {
            OkDownload.getInstance().getTask(currentProgress!!.tag)?.unRegister(currentProgress!!.tag)
        }
    }

    private fun startNextDownloadProgress() {
        val temp = taskQueue.poll()
        judgeCurrentProgressAdd()
        if (temp == null) {
            dismissProgressDialog()
            setDownloadingState()
            return
        }
        currentProgress = temp
        downloadingList.adapter?.notifyDataSetChanged()
    }

    private fun judgeCurrentProgressAdd() {
        if (currentProgress != null && currentProgress?.status != Progress.FINISH) {
            taskQueue.add(currentProgress)
        }
        currentProgress = null
    }

    private fun setListener(progress: Progress) {
        taskQueue.forEach {
            if (it.tag == currentProgress?.tag) {
                taskQueue.remove(it)
            }
        }
        OkDownload.getInstance().getTask(progress.tag)?.unRegister(progress.tag)
        OkDownload.getInstance().getTask(progress.tag)?.register(object : DownloadListener(progress.tag) {
            override fun onFinish(t: File, progress: Progress) {
                setListData()
                startNextDownloadProgress()
                setResult(Activity.RESULT_OK)
            }

            override fun onRemove(progress: Progress) {
                currentProgress?.status = Progress.FINISH
                startNextDownloadProgress()
            }

            override fun onProgress(progress: Progress) {
                currentHolder?.apply {
                    getView<ProgressBar>(R.id.downloadProgress).progress = (progress.fraction * 100).toInt()
                    setItemStatus(progress, getView(R.id.downloadSpeed), getView(R.id.downloadInfo))
                }
                currentProgress?.totalSize = progress.totalSize
                currentProgress?.currentSize = progress.currentSize
                currentProgress?.status = progress.status
                setDownloadingState()
                if (progress.status == Progress.LOADING || progress.status == Progress.PAUSE) {
                    dismissProgressDialog()
                }
            }

            override fun onError(progress: Progress) {
                currentProgress?.status = Progress.ERROR
                currentHolder?.apply {
                    setItemStatus(progress, getView(R.id.downloadSpeed), getView(R.id.downloadInfo))
                }
                dismissProgressDialog()
                currentProgress = null
                currentHolder = null
            }

            override fun onStart(progress: Progress) {
                currentProgress?.status = Progress.LOADING
                setDownloadingState()
            }
        })

        if (!isPauseAll && progress.status != Progress.LOADING) {
            OkDownload.getInstance().getTask(progress.tag)?.start()
        }
    }

    private fun checkDownloadingState(): Boolean {
        if (currentProgress?.status == Progress.LOADING) {
            return true
        }
        for (progress in taskQueue) {
            if (progress.status == Progress.LOADING) {
                return true
            }
        }
        return false
    }

    private fun setDownloadingState() {
        if (isEdit) {
            return
        }
        val status = checkDownloadingState()
        if (!status) {  // 处理开始
            downloadAction.setText(R.string.start_all)
            downloadAction.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_play_32, 0, 0, 0)
        } else {
            isPauseAll = false
            downloadAction.setText(R.string.stop_all)
            downloadAction.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_pause_32, 0, 0, 0)
        }
    }

    fun onDownloadAction(view: View?) {
        if (isEdit) {
            return
        }
        val status = checkDownloadingState()
        if (status) {
            isPauseAll = true
            OkDownload.getInstance().pauseAll()
            judgeCurrentProgressAdd()
            taskQueue.forEach { it.status = Progress.PAUSE }
            setDownloadingState()
            downloadingList.adapter?.notifyDataSetChanged()
        } else {
            showProgressDialog()
            isPauseAll = false
            startNextDownloadProgress()
        }
    }

    override fun onRightClick(view: View) {
        if (isEdit) {
            setRightText(R.string.edit)
            editContent.visibility = View.GONE
            leftImageView.visibility = View.VISIBLE
        } else {
            setRightText(R.string.cancel)
            editContent.visibility = View.VISIBLE
            leftImageView.visibility = View.INVISIBLE
        }
        isEdit = !isEdit
        selectedItem.clear()
        downloadingList.adapter?.notifyDataSetChanged()
        setDownloadingState()
    }

    private fun setRecord(data: MutableList<Progress>) {
        val adapter: BaseQuickAdapter<Progress, BaseViewHolder> =
            object : BaseQuickAdapter<Progress, BaseViewHolder>(R.layout.layout_downloading_item, data) {
                override fun convert(holder: BaseViewHolder, item: Progress) {
                    if (holder == currentHolder && item.tag != currentProgress?.tag) {
                        currentHolder = null
                    }
                    holder.itemView.tag = holder
                    if (item.tag == currentProgress?.tag) {
                        currentHolder = holder
                        if (item.status != Progress.LOADING)
                            setListener(currentProgress!!)
                    }
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
                    setItem(holder, item)
                }

            }
        adapter.setOnItemClickListener { adapter, view, position ->
            val progress = adapter.getItem(position) as Progress
            val holder = view.tag as BaseViewHolder
            if (!isEdit) {
                isPauseAll = false
                currentProgress?.let {
                    pauseProgress(it)
                    currentHolder?.let { it1 -> setItem(it1, it) }
                }
                OkDownload.getInstance().pauseAll()
                if (progress.tag == currentProgress?.tag) {
                    startNextDownloadProgress()
                } else {
                    showProgressDialog()
                    judgeCurrentProgressAdd()
                    this.currentHolder = holder
                    currentProgress = progress
                    setListener(progress)
                }
            } else {
                if (selectedItem.contains(progress)) {
                    selectedItem.remove(progress)
                    holder.getView<CheckBox>(R.id.item_check).isChecked = false
                } else {
                    selectedItem.add(progress)
                    holder.getView<CheckBox>(R.id.item_check).isChecked = true
                }
            }
        }
        downloadingList.adapter = adapter
    }

    private fun pauseProgress(progress: Progress) {
        OkDownload.getInstance().getTask(progress.tag)?.unRegister(progress.tag)
        OkDownload.getInstance().getTask(progress.tag)?.pause()
        progress.status = Progress.PAUSE
    }

    private fun setItem(holder: BaseViewHolder, item: Progress) {
        if (item.extra1 != null && item.extra1.toString().isNotEmpty()) {
            val bean = Gson().fromJson(item.extra1.toString(), VideoBaseBean::class.java)
            val image = holder.getView<ImageView>(R.id.image_film)
            image.layoutParams = LinearLayout.LayoutParams(
                SizeUtils.dp2px(110f),
                if (bean.videoType == Constant.VIDEO_SHORT) SizeUtils.dp2px(62f) else SizeUtils.dp2px(147f)
            )
            bean.coverImgUrl?.let { ImageUtil.loadImage(this, it, image) }
            holder.setText(R.id.film_name, bean.title)
            if (bean.episodeTitle.isNullOrEmpty()) {
                holder.setText(R.id.film_number, "")
            } else {
                holder.setText(R.id.film_number, bean.episodeTitle)
            }
        }
        holder.getView<ProgressBar>(R.id.downloadProgress).progress = (item.fraction * 100).toInt()
        if (item.totalSize == -1L) {
            item.totalSize = 0
        }
        setItemStatus(item, holder.getView(R.id.downloadSpeed), holder.getView(R.id.downloadInfo))
    }

    private fun setItemStatus(progress: Progress, speedText: TextView, infoText: TextView, waiting: Int = R.string.wait_download) {
        val totalSize = if (progress.totalSize > 0) progress.totalSize else 0
        when (progress.status) {
            Progress.PAUSE -> {
                speedText.setTextColor(ColorUtils.getColor(R.color.red))
                speedText.text = getString(R.string.pauseing)
                infoText.text = Html.fromHtml(
                    getString(
                        R.string.wait_download_info,
                        NormalUtil.formatFileSize(this, progress.currentSize),
                        NormalUtil.formatFileSize(this, totalSize)
                    )
                )
            }

            Progress.ERROR -> {
                speedText.setTextColor(ColorUtils.getColor(R.color.red))
                speedText.text = getString(R.string.download_error)
                infoText.text = Html.fromHtml(
                    getString(
                        R.string.wait_download_info,
                        NormalUtil.formatFileSize(this, progress.currentSize),
                        NormalUtil.formatFileSize(this, totalSize)
                    )
                )
            }

            Progress.LOADING -> {
                speedText.setTextColor(ColorUtils.getColor(R.color.blue))
                speedText.text = NormalUtil.formatFileSize(this, progress.speed) + "/s"
                infoText.text = Html.fromHtml(
                    getString(
                        R.string.downloading_info,
                        NormalUtil.formatFileSize(this, progress.currentSize),
                        NormalUtil.formatFileSize(this, totalSize)
                    )
                )
            }

            else -> {
                speedText.setTextColor(ColorUtils.getColor(R.color.grey))
                speedText.text = getString(waiting)
                if (progress.totalSize == 0L) {
                    infoText.text = Html.fromHtml(getString(R.string.wait_download_info, "-", "-"))
                } else {
                    infoText.text = Html.fromHtml(
                        getString(
                            R.string.wait_download_info,
                            NormalUtil.formatFileSize(this, progress.currentSize),
                            NormalUtil.formatFileSize(this, totalSize)
                        )
                    )
                }
            }
        }
    }

    private fun setEmpty() {
        currentHolder = null
        showEmpty()
        editContent.visibility = View.GONE
        topContent.visibility = View.GONE
        emptyLargeTip.text = getString(R.string.no_downloading_video)
        emptyLittleTip.text = getString(R.string.all_download_finish)
        setRightTextVisible(View.GONE)
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
            OkDownload.getInstance().pauseAll()
            val sb = StringBuffer()
            if (currentProgress != null) {
                OkDownload.getInstance().getTask(currentProgress?.tag)?.unRegister(currentProgress?.tag)
                deleteTask(currentProgress!!)
                val bean = Gson().fromJson(currentProgress!!.extra1.toString(), VideoBaseBean::class.java)
                if (bean != null) {
                    sb.append(bean.mediaKey + ";")
                }
                currentProgress = null
            }
            taskQueue.forEach {
                if (it.extra1 != null) {
                    val bean = Gson().fromJson(it.extra1.toString(), VideoBaseBean::class.java)
                    if (bean != null) {
                        sb.append(bean.mediaKey + ";")
                    }
                    deleteTask(it)
                }
            }
            cancelDownload(sb.substring(0, sb.length - 1))
            onRightClick(rightContent)
            setEmpty()
        }
        tipsDialog.show()
    }

    fun delete(view: View) {
        if (selectedItem.isNullOrEmpty()) {
            ToastUtils.showShort(R.string.select_data_tip)
            return
        }
        val tipsDialog = TipsDialog(this)
        tipsDialog.setDialogTitle(R.string.delete_record)
        tipsDialog.setMsg(R.string.delete_tips)
        tipsDialog.setLeftListener(R.string.save) {
            tipsDialog.dismiss()
        }
        tipsDialog.setRightListener(R.string.delete) {
            tipsDialog.dismiss()
//            OkDownload.getInstance().pauseAll()
            var isDownloading = false
            val sb = StringBuffer()
            for (progress in selectedItem) {
                if (currentProgress?.tag == progress.tag) {
                    OkDownload.getInstance().getTask(currentProgress?.tag)?.unRegister(currentProgress?.tag)
                    currentProgress = null
                    isDownloading = true
                }
                val bean = Gson().fromJson(progress.extra1.toString(), VideoBaseBean::class.java)
                if (bean != null) {
                    sb.append(bean.mediaKey + ";")
                }
                deleteTask(progress)
            }
            cancelDownload(sb.substring(0, sb.length - 1))
            onRightClick(rightContent)
            setListData()
            if (isDownloading && currentProgress == null) {
                startNextDownloadProgress()
            }
        }
        tipsDialog.show()
    }

    private fun cancelDownload(mediaIds: String) {
        val params = HttpParams()
        params.put("mediaKey", mediaIds)
        HttpRequest.get(RequestUrls.CANCEL_DOWNLOAD, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
            }

            override fun onError(response: String?, errorMsg: String?) {
            }
        }, params)
    }

    private fun deleteTask(progress: Progress) {
        DownloadMgr.deleteTask(progress.tag, true)
    }
}
package com.cqcsy.lgsp.views.dialog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.Gravity
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.*
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.VideoBaseBean
import com.cqcsy.lgsp.event.GetDownloadUrlEvent
import com.cqcsy.lgsp.event.TaskCancelEvent
import com.cqcsy.lgsp.download.DownloadMgr
import com.cqcsy.lgsp.download.DownloadUtil
import com.cqcsy.lgsp.offline.OfflineActivity
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.video.bean.ClarityBean
import com.cqcsy.library.download.server.DownloadListener
import com.cqcsy.library.download.server.OkDownload
import com.cqcsy.library.views.BottomBaseDialog
import com.lzy.okgo.db.DownloadManager
import com.lzy.okgo.model.Progress
import kotlinx.android.synthetic.main.layout_download_quality.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File

/**
 * 下载小视频、电影选择
 */
class DownloadSelectDialog : BottomBaseDialog {
    val clarityList: MutableList<ClarityBean>
    val videoBaseBean: VideoBaseBean
    val downloadListener: DownloadListener

    constructor(
        context: Context,
        baseBean: VideoBaseBean,
        clarityList: MutableList<ClarityBean>
    ) : super(context) {
        this.clarityList = clarityList
        this.videoBaseBean = baseBean.clone()
        downloadListener = object : DownloadListener(videoBaseBean.episodeKey) {
            override fun onStart(progress: Progress?) {
                refreshView()
            }

            override fun onProgress(progress: Progress?) {
                refreshView()
            }

            override fun onError(progress: Progress?) {
                refreshView()
            }

            override fun onFinish(t: File?, progress: Progress?) {
                refreshView()
            }

            override fun onRemove(progress: Progress?) {
                refreshView()
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_download_quality)
        setDownloadInfo()
        cancel.setOnClickListener { dismiss() }
        download_content.setOnClickListener {
            dismiss()
            context.startActivity(Intent(context, OfflineActivity::class.java))
        }
        setList()

        registerDownloadListener()
    }

    private fun setDownloadInfo() {
        sdcardLeft.text = Html.fromHtml(
            StringUtils.getString(
                R.string.card_left,
                NormalUtil.getTotalMemorySize(context)
            )
        )
        downloadNumber.text = DownloadMgr.getDownloadingSize().toString()
    }

    private fun refreshView() {
        qualityList.adapter?.notifyDataSetChanged()
        setDownloadInfo()
    }

    private fun setList() {
        qualityList.layoutManager = LinearLayoutManager(context)
        val adapter =
            object : BaseQuickAdapter<ClarityBean, BaseViewHolder>(
                R.layout.layout_center_text,
                clarityList
            ) {
                override fun convert(holder: BaseViewHolder, item: ClarityBean) {
                    val itemName = holder.getView<TextView>(R.id.item_text)
                    if (!item.resolutionDes.isNullOrEmpty()) {
                        itemName.text = item.resolutionDes
                    } else {
                        itemName.text = item.title
                    }
                    val itemStatus = holder.getView<ImageView>(R.id.status_image)
                    val task = DownloadManager.getInstance().get(item.episodeKey)
                    if (task != null) {
                        itemName.setTextColor(
                            ColorUtils.getColor(R.color.word_color_2)
                        )
                        if (task.status == Progress.FINISH) {
                            itemStatus.setImageResource(R.mipmap.icon_download_finished)
                        } else {
                            itemStatus.setImageResource(R.mipmap.icon_downloading)
                        }
                    } else {
                        itemStatus.setImageBitmap(null)
                        itemName.setTextColor(ColorUtils.getColor(R.color.grey))
                    }
                    val itemContainer = holder.getView<RelativeLayout>(R.id.item_container)
                    val paramsText = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    )
                    if (!item.resolutionDes.isNullOrEmpty()) {
                        paramsText.addRule(RelativeLayout.CENTER_IN_PARENT)
                        val itemParams = FrameLayout.LayoutParams(
                            SizeUtils.dp2px(160f),
                            SizeUtils.dp2px(56f)
                        )
                        itemParams.bottomMargin = SizeUtils.dp2px(10f)
                        itemParams.gravity = Gravity.CENTER_HORIZONTAL
                        itemContainer.layoutParams = itemParams
                    } else {
                        paramsText.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                        paramsText.addRule(RelativeLayout.CENTER_VERTICAL)
                        paramsText.leftMargin = SizeUtils.dp2px(14f)
                    }
                    itemName.layoutParams = paramsText
                }

            }
        adapter.setOnItemClickListener { adapter, view, position ->
            val result = Utils.Consumer<Boolean> {
                if (it) {
                    val item = adapter.getItem(position) as ClarityBean
                    videoBaseBean.resolution = item.resolution
                    videoBaseBean.resolutionDes = item.resolutionDes
                    videoBaseBean.lang = item.lang
//                    if (videoBaseBean.videoType == Constant.VIDEO_SHORT) {
//                        DownloadMgr.startDownload(context, videoBaseBean)
//                    } else {
                    DownloadUtil.getPlayInfo(context, videoBaseBean)
//                    }
                    Handler().postDelayed({ adapter.notifyDataSetChanged() }, 2000)
                } else {
                    dismiss()
                    DownloadUtil.showNetTip(context, videoBaseBean)
                }
            }
            NetworkUtils.isWifiAvailableAsync(result)
        }
        qualityList.adapter = adapter
    }

    override fun show() {
        super.show()
        EventBus.getDefault().register(this)
    }

    override fun dismiss() {
        super.dismiss()
        EventBus.getDefault().unregister(this)
        OkDownload.getInstance().getTask(videoBaseBean.episodeKey)?.unRegister(downloadListener)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDownloadStatusChange(event: GetDownloadUrlEvent) {
        registerDownloadListener()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaskCancel(event: TaskCancelEvent) {
        Handler().postDelayed({
            OkDownload.getInstance().getTask(event.taskTag)?.unRegister(downloadListener)
        }, 500)
    }

    private fun registerDownloadListener() {
        Handler().postDelayed({
            OkDownload.getInstance().getTask(videoBaseBean.episodeKey)?.register(downloadListener)
        }, 500)
    }
}
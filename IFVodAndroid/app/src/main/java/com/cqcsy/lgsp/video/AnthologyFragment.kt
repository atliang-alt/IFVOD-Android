package com.cqcsy.lgsp.video

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.Utils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.VideoBaseBean
import com.cqcsy.lgsp.download.DownloadMgr
import com.cqcsy.lgsp.event.GetDownloadUrlEvent
import com.cqcsy.lgsp.event.TaskCancelEvent
import com.cqcsy.lgsp.vip.OpenVipActivity
import com.cqcsy.library.base.NormalFragment
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.views.TipsDialog
import com.google.gson.Gson
import com.littlejerk.rvdivider.builder.XGridBuilder
import com.lzy.okgo.model.Progress
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import kotlinx.android.synthetic.main.layout_anthology_fragment.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 选集详情页Fragment
 */
class AnthologyFragment : NormalFragment() {
    var dataList: MutableList<VideoBaseBean>? = null
    var type = 0
    var uniqueId = 0
    var action = -1
    var coverImage: String = ""
    var downloadList: MutableList<Progress>? = null
    var listener: onDownloadStart? = null

    // 用于判断选集样式 true为综艺样式显示，false剧集样式显示
    private var isVarietyView = false

    fun setDownloadListener(l: onDownloadStart) {
        this.listener = l
    }

    interface onDownloadStart {
        fun onStartDownload()

        fun getCurrentSharpness(): Int
    }

    override fun getContainerView(): Int {
        return R.layout.layout_anthology_fragment
    }

    override fun initData() {
        dataList = ArrayList()
    }

    override fun onResume() {
        super.onResume()
        if (action > 0) {
            downloadList = DownloadMgr.getAllDownload()
        }
        anthologyRecycle.adapter?.notifyDataSetChanged()
    }

    fun setFragmentData(
        type: Int,
        action: Int,
        uniqueId: Int,
        coverImage: String,
        isVarietyView: Boolean,
        data: MutableList<VideoBaseBean>
    ) {
        dataList?.clear()
        dataList?.addAll(data)
        this.type = type
        this.action = action
        this.uniqueId = uniqueId
        this.coverImage = coverImage
        this.isVarietyView = isVarietyView
        if (action > 0) {
            downloadList = DownloadMgr.getAllDownload()
        }
        initView()
    }

    override fun initView() {
        for (i in 0 until anthologyRecycle.itemDecorationCount) {
            anthologyRecycle.removeItemDecorationAt(i)
        }
        anthologyRecycle.layoutManager = null
        if (isVarietyView) {
            anthologyRecycle.layoutManager = GridLayoutManager(context, 2)
            anthologyRecycle.addItemDecoration(
                XGridBuilder(requireContext()).setVLineSpacing(10f).setHLineSpacing(10f)
                    .setIncludeEdge(true).build()
            )
        } else {
            anthologyRecycle.layoutManager = GridLayoutManager(context, 5)
            anthologyRecycle.addItemDecoration(
                XGridBuilder(requireContext()).setVLineSpacing(15f).setHLineSpacing(15f)
                    .setIncludeEdge(true).build()
            )
        }
        val svgaParser = SVGAParser(requireContext())
        val adapter = object : BaseQuickAdapter<VideoBaseBean, BaseViewHolder>(
            R.layout.item_variety_anthology,
            dataList
        ) {
            override fun convert(holder: BaseViewHolder, item: VideoBaseBean) {
                val textView = holder.getView<TextView>(R.id.anthologyNumb)
                val playView = holder.getView<SVGAImageView>(R.id.playerImage)
                if (isVarietyView) {
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                } else {
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                }
                if (uniqueId == item.uniqueID) {
                    var animName = "playing_blue.svga"
                    if (item.isLive) {
                        animName = "playing_live.svga"
                    }
                    svgaParser.decodeFromAssets(animName, object : SVGAParser.ParseCompletion {
                        override fun onComplete(videoItem: SVGAVideoEntity) {
                            playView.setImageDrawable(SVGADrawable(videoItem))
                            playView.startAnimation()
                        }

                        override fun onError() {
                        }
                    })
                    textView.isSelected = true
                    playView.visibility = View.VISIBLE
                } else {
                    textView.isSelected = false
                    playView.visibility = View.GONE
                    playView.stopAnimation()
                }
                textView.text = item.episodeTitle
                setVipData(holder.getView(R.id.vipImage), item)
                textView.setOnClickListener {
                    backVideoInfo(item)
                }
                val downloadState = holder.getView<ImageView>(R.id.status_image)
                val status = checkStatus(item)
                if (action > 0 && status >= 0) {
                    downloadState.visibility = View.VISIBLE
                    if (status == Progress.FINISH) {
                        downloadState.setImageResource(R.mipmap.icon_download_finished)
                    } else {
                        downloadState.setImageResource(R.mipmap.icon_downloading)
                    }
                } else {
                    downloadState.visibility = View.GONE
                }
            }
        }
        anthologyRecycle.adapter = adapter
        initPlayIndex()
    }

    private fun initPlayIndex() {
        val playIndex = dataList?.indexOfFirst { it.uniqueID == uniqueId } ?: -1
        if (playIndex != -1) {
            Handler().post { anthologyRecycle?.scrollToPosition(playIndex) }
        }
    }

    private fun checkStatus(item: VideoBaseBean): Int {
        if (downloadList == null || downloadList!!.size == 0) {
            return -1
        }
        for (progress in downloadList!!) {
            if (progress.extra1 != null && progress.extra1.toString().isNotEmpty()) {
                val bean = Gson().fromJson(progress.extra1.toString(), VideoBaseBean::class.java)
                if (item.uniqueID == bean.uniqueID) {
                    return progress.status
                }
            }
        }
        return -1
    }

    /**
     * 点击选集返回播放详情页
     */
    private fun backVideoInfo(videoBaseBean: VideoBaseBean) {
        if (action > 0) {
            if (!GlobalValue.isVipUser()) {
                openVip()
                return
            }
            videoBaseBean.coverImgUrl = coverImage
            val result = Utils.Consumer<Boolean> {
                if (it) {
                    startDownload(videoBaseBean)
                } else {
                    showNetTip(videoBaseBean)
                }
            }
            NetworkUtils.isWifiAvailableAsync(result)
        } else {
            val intent = Intent()
            intent.putExtra("anthologyBean", videoBaseBean)
            activity?.setResult(Activity.RESULT_OK, intent)
            activity?.finish()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDownloadStart(event: GetDownloadUrlEvent) {
        handler.postDelayed(updateDownload, 1000)
    }

    private fun startDownload(videoBean: VideoBaseBean) {
        if (!GlobalValue.isVipUser()) {
            openVip()
        } else if (videoBean.isLive) {
            ToastUtils.showShort(R.string.live_download_tip)
        } else {
            DownloadMgr.startDownload(requireContext(), videoBean)
        }
    }

    private fun openVip() {
        val intent = Intent(requireContext(), OpenVipActivity::class.java)
        intent.putExtra("pathInfo", requireActivity().javaClass.simpleName)
        startActivity(intent)
    }

    private fun showNetTip(videoBean: VideoBaseBean) {
        val dialog = TipsDialog(requireContext())
        dialog.setDialogTitle(R.string.tips)
        dialog.setMsg(R.string.no_wifi_tip)
        dialog.setLeftListener(R.string.cancel) {
            dialog.dismiss()
        }
        dialog.setRightListener(R.string.continue_download) {
            dialog.dismiss()
            startDownload(videoBean)
        }
        dialog.show()
    }

    private val handler = Handler()
    private val updateDownload = Runnable {
        downloadList = DownloadMgr.getAllDownload()
        listener?.onStartDownload()
        anthologyRecycle.adapter?.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateDownload)
    }

    /**
     * 设置每个Item的Vip、最新之类数据
     */
    private fun setVipData(view: ImageView, videoBaseBean: VideoBaseBean) {
        if (!videoBaseBean.isLast) {
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaskCancel(event: TaskCancelEvent) {
        if (downloadList != null) {
            for (item in downloadList!!) {
                if (item.tag == event.taskTag) {
                    downloadList?.remove(item)
                    break
                }
            }
            anthologyRecycle.adapter?.notifyDataSetChanged()
            listener?.onStartDownload()
        }
    }
}
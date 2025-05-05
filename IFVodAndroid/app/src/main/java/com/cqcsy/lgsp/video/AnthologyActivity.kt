package com.cqcsy.lgsp.video

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.lgsp.bean.VideoBaseBean
import com.cqcsy.lgsp.download.DownloadMgr
import com.cqcsy.lgsp.offline.OfflineActivity
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.video.bean.ClarityBean
import com.cqcsy.lgsp.video.bean.VideoGroupBean
import com.cqcsy.lgsp.video.viewModel.VideoViewModel
import com.cqcsy.library.views.BottomBaseDialog
import com.cqcsy.library.utils.Constant
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_anthology.*

/**
 * 选集详情页
 * 电视剧、综艺
 */
class AnthologyActivity : NormalActivity(), AnthologyFragment.onDownloadStart {
    val total = 50
    private var videoGroupList: MutableList<VideoGroupBean> = ArrayList()
    var pageAction = 0

    var mediaKey = ""
    var resolution = ""
    var episodeTitle = ""
    var lang = ""
    var videoType = 0
    var uniqueId = 0

    // 用于判断选集样式 true为综艺样式显示，false剧集样式显示
    private var isVarietyView = false

    private val mViewModel: VideoViewModel by viewModels()

    override fun getContainerView(): Int {
        return R.layout.activity_anthology
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initObserve()
        initData()
        if (videoType == Constant.VIDEO_TV) {
            setHeaderTitle(R.string.select_tv_channel)
        } else {
            setHeaderTitle(resources.getString(R.string.video_show))
        }
    }

    private fun initObserve() {
        mViewModel.mEpisodeList.observe(this) {
            dismissProgressDialog()
            if (it.first) {
                videoGroupList = AnthologyDataUtil.getGroupData(videoType, it.second, uniqueId)
                initPager()
            } else {
                showFailed {
                    showProgressDialog()
                    mViewModel.getEpisodeInfo(mediaKey, resolution)
                }
            }
        }
        mViewModel.mResolutionList.observe(this) {
            if (!it.isNullOrEmpty()) {
                chooseQuality.visibility = View.VISIBLE
                var clarityBean: ClarityBean? = null
                if (resolution.isEmpty()) {
                    clarityBean = it[0]
                    resolution = it[0].resolution.toString()
                    mViewModel.getEpisodeInfo(mediaKey, resolution)
                } else {
                    for (item in it) {
                        if (item.resolution == resolution) {
                            clarityBean = item
                            break
                        }
                    }
                }
                if (clarityBean != null) {
                    chooseQuality.tag = clarityBean
                    chooseQuality.text = Html.fromHtml(getString(R.string.choose_down_quality, clarityBean.resolutionDes))
                }
            }
        }
    }

    private fun initData() {
        mediaKey = intent.getStringExtra("mediaKey") ?: ""
        episodeTitle = intent.getStringExtra("episodeTitle") ?: ""
        lang = intent.getStringExtra("lang") ?: ""
        videoType = intent.getIntExtra("videoType", 0)
        uniqueId = intent.getIntExtra("uniqueId", 0)
        pageAction = intent.getIntExtra("pageAction", 0)
        if (pageAction > 0) {
//            if (resolution.isEmpty()) {
//                resolution = "480"
//            }
            setHeaderTitle(R.string.choose_down_video)
            resolution = intent.getStringExtra("resolution") ?: ""
            bottomSdcard.visibility = View.VISIBLE
            downloadNumber.text = DownloadMgr.getDownloadingSize().toString()
            sdcardLeft.text =
                Html.fromHtml(getString(R.string.card_left, NormalUtil.getTotalMemorySize(this)))
            mViewModel.getAllResolution(mediaKey)
            (eachPage as AnthologyFragment).setDownloadListener(this)
        }
        showProgressDialog()
        mViewModel.getEpisodeInfo(mediaKey, resolution)
    }

    override fun onResume() {
        super.onResume()
        if (bottomSdcard.isVisible) {
            downloadNumber.text = DownloadMgr.getDownloadingSize().toString()
        }
    }

    private fun initPager() {
        // 选集标题长度大于5，显示样式为综艺样式布局
        for (i in videoGroupList.indices) {
            val bean = videoGroupList[i].itemList?.filter { (it.episodeTitle?.length ?: 0) > 5 }
            if (!bean.isNullOrEmpty()) {
                isVarietyView = true
                break
            } else {
                isVarietyView = false
            }
        }
        if (videoGroupList.size == 1) {
            tabLayout.visibility = View.GONE
            viewLine.visibility = View.GONE
            setFragmentData(videoGroupList[0].itemList!!)
        } else {
            tabLayout.visibility = View.VISIBLE
            viewLine.visibility = View.VISIBLE
            setTabs()
        }
    }

    private fun setTabs() {
        var currentPosition = 0
        tabLayout.removeAllTabs()
        for (i in videoGroupList.indices) {
            tabLayout.addTab(tabLayout.newTab().setText(videoGroupList[i].groupName))
            if (videoGroupList[i].isExpand) {
                currentPosition = i
            }
        }
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position ?: 0
                if (tab != null && position < videoGroupList.size && !videoGroupList[position].itemList.isNullOrEmpty()) {
                    setFragmentData(videoGroupList[position].itemList!!)
                }
                NormalUtil.clearTabLayoutTips(tabLayout)
            }

        })
        tabLayout.getTabAt(currentPosition)?.select()
        setFragmentData(videoGroupList[currentPosition].itemList!!)
        Handler().post { tabLayout.smoothScrollTo(SizeUtils.dp2px(90f) * currentPosition, 0) }
    }

    private fun setFragmentData(data: MutableList<VideoBaseBean>) {
        (eachPage as AnthologyFragment).setFragmentData(
            videoType,
            pageAction,
            uniqueId,
            intent.getStringExtra("coverImage") ?: "",
            isVarietyView,
            data
        )
    }

    fun showSelectQuality(view: View) {
        if (mViewModel.mResolutionList.value.isNullOrEmpty()) {
            ToastUtils.showShort(R.string.get_quality_failed)
            return
        }
        val dialog = object : BottomBaseDialog(this) {}
        val contentView = View.inflate(this, R.layout.layout_quality_select, null)
        contentView.findViewById<TextView>(R.id.cancel).setOnClickListener { dialog.dismiss() }
        val list = contentView.findViewById<RecyclerView>(R.id.qualityList)
        list.layoutManager = LinearLayoutManager(this)
        val adapter = object : BaseQuickAdapter<ClarityBean, BaseViewHolder>(R.layout.layout_center_text, mViewModel.mResolutionList.value) {
            override fun convert(holder: BaseViewHolder, item: ClarityBean) {
                holder.getView<RelativeLayout>(R.id.item_container).setBackgroundColor(Color.TRANSPARENT)
                holder.setText(R.id.item_text, item.resolutionDes)
                holder.getView<ImageView>(R.id.status_image).visibility = View.GONE
            }

        }
        adapter.setOnItemClickListener { adapter, view, position ->
            dialog.dismiss()
            val resolution = adapter.getItem(position) as ClarityBean
            if (resolution != chooseQuality.tag) {
                chooseQuality.tag = resolution
                chooseQuality.text = Html.fromHtml(getString(R.string.choose_down_quality, resolution.resolutionDes))
                resolution.resolution?.let {
                    mViewModel.getEpisodeInfo(mediaKey, it)
                }
            }
        }
        list.adapter = adapter
        dialog.setContentView(contentView)
        dialog.show()
    }

    fun downloading(view: View) {
        startActivity(Intent(this, OfflineActivity::class.java))
    }

    override fun onStartDownload() {
        downloadNumber.text = DownloadMgr.getDownloadingSize().toString()
    }

    override fun getCurrentSharpness(): Int {
        if (chooseQuality.tag != null && chooseQuality.tag is Int) {
            return chooseQuality.tag.toString().toInt()
        }
        return 480
    }
}
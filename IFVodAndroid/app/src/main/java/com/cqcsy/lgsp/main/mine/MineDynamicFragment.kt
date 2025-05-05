package com.cqcsy.lgsp.main.mine

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.blankj.utilcode.util.SizeUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.DynamicBean
import com.cqcsy.lgsp.database.bean.DynamicCacheBean
import com.cqcsy.lgsp.database.manger.DynamicCacheManger
import com.cqcsy.lgsp.event.*
import com.cqcsy.library.base.refresh.RefreshFragment
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.views.LoadingView
import com.cqcsy.library.views.TipsDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.fragment_mine_dynamic.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * 我的动态fragment
 */
class MineDynamicFragment : RefreshFragment() {
    var userId: Int = 0
    private lateinit var dynamicAdapter: MineDynamicListAdapter
    private lateinit var releaseFailAdapter: MineDynamicFailAdapter
    private lateinit var releasingAdapter: MineDynamicReleasingAdapter
    private val releaseFailList: MutableList<DynamicCacheBean> = mutableListOf()
    private val releasingList: MutableList<DynamicCacheBean> = mutableListOf()
    private lateinit var decoration: DividerItemDecoration
    override fun getRefreshChild(): Int {
        return R.layout.fragment_mine_dynamic
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments?.getInt("userId") != null) {
            userId = arguments?.getInt("userId")!!
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emptyLargeTip.setText(R.string.empty_tip)
        refreshLayout?.setPadding(SizeUtils.dp2px(12f), 0, SizeUtils.dp2px(12f), 0)
        initReleaseFailList()
        initReleasingList()
        initDynamicList()
        showProgress()
        loadData()
        selectReleaseDynamicCache()
    }

    private fun initReleaseFailList() {
        releaseFailAdapter = MineDynamicFailAdapter()
        releaseFailAdapter.addChildClickViewIds(R.id.iv_delete, R.id.iv_resend)
        releaseFailAdapter.setOnItemChildClickListener { _, view, position ->
            val item = releaseFailAdapter.getItem(position)
            when (view.id) {
                R.id.iv_delete -> {
                    showDeleteDialog(item.id, true)
                }

                R.id.iv_resend -> {
                    ReleaseDynamicVideoActivity.launch(this, item, 11111)
                    DynamicCacheManger.instance.delete(item.id)
                    selectReleaseFail()
                }
            }
        }
        releaseFailAdapter.setOnItemClickListener { _, _, position ->
            val item = releaseFailAdapter.getItem(position)
            ReleaseDynamicVideoActivity.launch(this, item, 11111)
        }
        rv_release_fail_dynamic.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)
                outRect.top = SizeUtils.dp2px(5f)
            }
        })
        rv_release_fail_dynamic.adapter = releaseFailAdapter
    }

    private fun initReleasingList() {
        releasingAdapter = MineDynamicReleasingAdapter()
        releasingAdapter.addChildClickViewIds(R.id.iv_delete)
        releasingAdapter.setOnItemChildClickListener { _, view, position ->
            val item = releasingAdapter.getItem(position)
            when (view.id) {
                R.id.iv_delete -> {
                    showDeleteDialog(item.id, false)
                }
            }
        }
        (rv_releasing_dynamic.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        rv_releasing_dynamic.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)
                outRect.top = SizeUtils.dp2px(5f)
            }
        })
        rv_releasing_dynamic.adapter = releasingAdapter
    }

    private fun initDynamicList() {
        decoration = DividerItemDecoration(context, LinearLayout.VERTICAL)
        ResourcesCompat.getDrawable(resources, R.drawable.line_divider, null)
            ?.let { decoration.setDrawable(it) }
        dynamicAdapter = MineDynamicListAdapter()
        dynamicAdapter.setOnItemClickListener { _, _, position ->
            val item = dynamicAdapter.getItem(position)
            DynamicDetailsActivity.launch(context) {
                mediaKey = item.mediaKey ?: ""
                dynamicType = item.photoType
                videoIndex = 0
                dynamicVideoList = mutableListOf(item)
                openRecommend = false
                isFromMineDynamic = true
                fromUpperHomePage = true
                upperId = item.uid
            }
        }
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)
                val position = parent.getChildAdapterPosition(view)
                outRect.bottom = SizeUtils.dp2px(15f)
                if (position == 0) {
                    outRect.top = SizeUtils.dp2px(15f)
                }
            }
        })
        recyclerView.adapter = dynamicAdapter
    }

    private fun selectReleaseDynamicCache() {
        val selectAllData = DynamicCacheManger.instance.selectAllData()
        releaseFailList.clear()
        releasingList.clear()
        for (selectAllDatum in selectAllData) {
            if (selectAllDatum.status == DynamicReleaseStatus.RELEASE_FAIL) {
                releaseFailList.add(0, selectAllDatum)
            } else if (selectAllDatum.status == DynamicReleaseStatus.RELEASING) {
                releasingList.add(0, selectAllDatum)
            }
        }
        setReleaseFail()
        setReleasing()
    }

    private fun selectReleaseFail() {
        val selectData =
            DynamicCacheManger.instance.selectByStatus(DynamicReleaseStatus.RELEASE_FAIL)
        releaseFailList.clear()
        releaseFailList.addAll(selectData)
        releaseFailList.reverse()
        setReleaseFail()
    }

    private fun selectReleasing() {
        val selectData = DynamicCacheManger.instance.selectByStatus(DynamicReleaseStatus.RELEASING)
        releasingList.clear()
        releasingList.addAll(selectData)
        releasingList.reverse()
        setReleasing()
    }

    private fun setReleaseFail() {
        if (releaseFailList.isEmpty()) {
            rv_release_fail_dynamic.isVisible = false
            release_fail_text.isVisible = false
            return
        }
        rv_release_fail_dynamic.isVisible = true
        release_fail_text.isVisible = true
        releaseFailAdapter.setList(releaseFailList)
    }

    private fun setReleasing() {
        if (releasingList.isEmpty()) {
            rv_releasing_dynamic.isVisible = false
            releasing_text.isVisible = false
            return
        }
        rv_releasing_dynamic.isVisible = true
        releasing_text.isVisible = true
        releasingAdapter.setList(releasingList)
    }

    private fun loadData() {
        val params = HttpParams()
        params.put("page", page)
        params.put("size", size)
        params.put("userId", userId)
        HttpRequest.get(
            RequestUrls.UPPER_DYNAMIC,
            object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                    dismissProgress()
                    finishRefresh()
                    val jsonArray = response?.optJSONArray("list")
                    val list: List<DynamicBean>? = Gson().fromJson(
                        jsonArray?.toString(),
                        object : TypeToken<List<DynamicBean>>() {}.type
                    )
                    if (list.isNullOrEmpty()) {
                        if (page == 1) {
                            dynamicAdapter.setList(list)
                            showEmpty()
                        } else {
                            page--
                            finishLoadMoreWithNoMoreData()
                        }
                    } else {
                        recyclerView.removeItemDecoration(decoration)
                        recyclerView.addItemDecoration(decoration)
                        if (page == 1) {
                            dynamicAdapter.setList(list)
                        } else {
                            dynamicAdapter.addData(list)
                            if (list.isEmpty()) {
                                finishLoadMoreWithNoMoreData()
                            } else {
                                finishLoadMore()
                            }
                        }
                    }
                }

                override fun onError(response: String?, errorMsg: String?) {
                    if (page > 1) {
                        page--
                    }
                    showFailed {
                        showProgress()
                        loadData()
                    }
                }
            },
            params,
            this
        )
    }

    override fun onRefresh() {
        page = 1
        loadData()
        selectReleaseDynamicCache()
    }

    override fun onLoadMore() {
        page++
        loadData()
    }

    private fun showDeleteDialog(id: Int, isDeleteFail: Boolean) {
        val dialog = TipsDialog(requireContext())
        dialog.setDialogTitle(R.string.cancel_release_dynamic_tip)
        dialog.setMsg(R.string.cancel_release_dynamic_message)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setLeftListener(R.string.cancel_release_dynamic_tip) {
            dialog.dismiss()
            DynamicCacheManger.instance.delete(id)
            if (isDeleteFail) {
                selectReleaseFail()
            } else {
                ReleaseDynamicService.stop(requireContext())
                selectReleasing()
            }
        }
        dialog.setRightListener(R.string.think) {
            dialog.dismiss()
        }
        dialog.show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReleaseDynamicChange(event: ReleaseDynamicEvent) {
        when (event.action) {
            DynamicReleaseStatus.RELEASING -> {
                selectReleaseDynamicCache()
//                csl_container.scrollToChild(releasing_text)
            }

            DynamicReleaseStatus.RELEASE_SUCCESS -> {
                selectReleaseDynamicCache()
                onRefresh()
            }

            DynamicReleaseStatus.RELEASE_FAIL -> {
                selectReleaseDynamicCache()
//                csl_container.scrollToChild(release_fail_text)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUploadEvent(event: UploadListenerEvent) {
        if (event.event == UploadListenerEvent.onProgress) {
            val bean = event.uploadCacheBean
            val progress = ((bean.progress * 100f) / bean.videoSize).toInt()
            for ((i, data) in releasingAdapter.data.withIndex()) {
                if (data.videoPath == bean.path && data.description == bean.context) {
                    //这个地方的判断很不严谨
                    releasingAdapter.data[i].progress = progress
                    releasingAdapter.notifyItemChanged(i)
                    break
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDynamicChange(event: DynamicEvent) {
        val dynamicBean = event.dynamicBean
        when (event.action) {
            DynamicEvent.DYNAMIC_ADD -> {
                if (dynamicBean != null) {
                    if (dynamicAdapter.data.isEmpty()) {
                        dismissProgress()
                    }
                    if (dynamicAdapter.data.isEmpty()) {
                        dynamicAdapter.addData(dynamicBean)
                    } else {
                        dynamicAdapter.addData(0, dynamicBean)
                        recyclerView?.smoothScrollToPosition(0)
                    }
                }
            }

            DynamicEvent.DYNAMIC_REMOVE -> {
                for ((i, data) in dynamicAdapter.data.withIndex()) {
                    if (data.mediaKey == dynamicBean?.mediaKey) {
                        dynamicAdapter.removeAt(i)
                        break
                    }
                }
                if (dynamicAdapter.data.isEmpty()) {
                    showEmpty()
                }
            }

            DynamicEvent.DYNAMIC_UPDATE -> {
                if (dynamicBean != null && dynamicAdapter.data.isNotEmpty()) {
                    for ((i, data) in dynamicAdapter.data.withIndex()) {
                        if (data.mediaKey == dynamicBean.mediaKey) {
                            data.copy(dynamicBean)
                            dynamicAdapter.notifyItemChanged(i)
                            break
                        }
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onZanChange(event: VideoActionResultEvent) {
        if (event.type != 2 || event.isCommentLike) {
            return
        }
        for ((i, data) in dynamicAdapter.data.withIndex()) {
            if (data.mediaKey.toString() == event.id) {
                data.likeCount = event.count
                data.like = event.selected
                dynamicAdapter.notifyItemChanged(i)
                break
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommentChange(event: CommentEvent) {
        for ((i, data) in dynamicAdapter.data.withIndex()) {
            if (data.mediaKey == event.mediaKey) {
                data.comments = data.comments + 1
                dynamicAdapter.notifyItemChanged(i)
                break
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBlackListChange(event: BlackListEvent) {
        for (data in dynamicAdapter.data) {
            if (data.uid == event.uid) {
                data.isBlackList = event.status
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFocusChange(event: VideoActionResultEvent) {
        if (event.type == 1) {
            for (data in dynamicAdapter.data) {
                if (data.uid.toString() == event.id) {
                    data.focus = event.action == VideoActionResultEvent.ACTION_ADD
                    break
                }
            }
        }
    }

    override fun showEmpty() {
        if (isSafe()) {
            dismissProgress()
            recyclerView.removeItemDecoration(decoration)
            val view = View.inflate(requireContext(), R.layout.layout_status_empty, null)
            view.findViewById<ImageView>(R.id.image_empty).setImageDrawable(emptyImage.drawable)
            view.findViewById<TextView>(R.id.large_tip).text = emptyLargeTip.text
            view.findViewById<TextView>(R.id.little_tip).text = emptyLittleTip.text
            dynamicAdapter.setEmptyView(view)
        }
    }


    override fun showFailed(listener: View.OnClickListener) {
        if (isSafe()) {
            dismissProgress()
            recyclerView.removeItemDecoration(decoration)
            val view = LoadingView(requireContext())
            view.showFailed(listener)
            dynamicAdapter.setEmptyView(view)
        }
    }
}
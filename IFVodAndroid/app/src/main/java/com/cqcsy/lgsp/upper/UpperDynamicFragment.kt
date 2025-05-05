package com.cqcsy.lgsp.upper

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.DynamicBean
import com.cqcsy.lgsp.event.BlackListEvent
import com.cqcsy.lgsp.event.CommentEvent
import com.cqcsy.lgsp.event.DynamicEvent
import com.cqcsy.lgsp.event.VideoActionResultEvent
import com.cqcsy.lgsp.main.mine.DynamicDetailsActivity
import com.cqcsy.library.base.refresh.RefreshFragment
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.views.LoadingView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_recyclerview.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * 动态fragment
 */
class UpperDynamicFragment : RefreshFragment() {
    var userId: Int = 0
    private val mDynamicList: MutableList<DynamicBean> = ArrayList()
    private var showLoading: Boolean = false
    private var isMineIn: Boolean = false

    override fun getRefreshChild(): Int {
        return R.layout.layout_recyclerview
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments?.getInt("userId") != null) {
            userId = arguments?.getInt("userId")!!
        }
        isMineIn = arguments?.getBoolean("isMineIn") ?: false
        showLoading = arguments?.getBoolean("showLoading", false) == true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emptyLargeTip.setText(R.string.empty_tip)
        refreshLayout?.setPadding(SizeUtils.dp2px(12f), 0, SizeUtils.dp2px(12f), 0)

        recyclerView.isNestedScrollingEnabled = true
        recyclerView.layoutManager = LinearLayoutManager(context)
        val decoration = DividerItemDecoration(context, LinearLayout.VERTICAL)
        ResourcesCompat.getDrawable(resources, R.drawable.line_divider, null)
            ?.let { decoration.setDrawable(it) }
        recyclerView.addItemDecoration(decoration)

        val adapter = DynamicListAdapter(mDynamicList)
        adapter.setOnItemClickListener { _, _, position ->
            val item = mDynamicList[position]
            if (!isMineIn) {
                item.viewCount += item.photoCount
                adapter.notifyItemChanged(position)
            }
            DynamicDetailsActivity.launch(context) {
                mediaKey = item.mediaKey ?: ""
                videoIndex = 0
                dynamicVideoList = mutableListOf(item)
                dynamicType = item.photoType
                openRecommend = false
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
        recyclerView.adapter = adapter
        if (showLoading) {
            showProgress()
            loadData()
        } else {
            showRefresh()
        }
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
                    if (page == 1) {
                        mDynamicList.clear()
                        recyclerView?.adapter?.notifyDataSetChanged()
                        finishRefresh()
                        dismissProgress()
                    }
                    val jsonArray = response?.optJSONArray("list")
                    if (jsonArray == null || jsonArray.length() == 0) {
                        if (page == 1) {
                            showEmpty()
                        } else {
                            page--
                            finishLoadMoreWithNoMoreData()
                        }
                        return
                    }
                    val list: List<DynamicBean> = Gson().fromJson(
                        jsonArray.toString(),
                        object : TypeToken<List<DynamicBean>>() {}.type
                    )
                    mDynamicList.addAll(list)
                    if (page == 1) {
                        recyclerView?.adapter?.notifyDataSetChanged()
                    } else {
                        recyclerView?.adapter?.notifyItemRangeChanged(
                            mDynamicList.size - list.size + 1,
                            mDynamicList.size
                        )
                    }
                    if (list.isNullOrEmpty()) {
                        finishLoadMoreWithNoMoreData()
                    } else {
                        finishLoadMore()
                    }
                }

                override fun onError(response: String?, errorMsg: String?) {
                    showFailed {
                        showRefresh()
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
    }

    override fun onLoadMore() {
        page++
        loadData()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDynamicChange(event: DynamicEvent) {
        when (event.action) {
            DynamicEvent.DYNAMIC_ADD -> {
                if (event.dynamicBean != null) {
                    if (mDynamicList.isEmpty()) {
                        dismissProgress()
                    }
                    if (mDynamicList.isEmpty()) {
                        mDynamicList.add(event.dynamicBean!!)
                        recyclerView?.adapter?.notifyItemChanged(0)
                    } else {
                        mDynamicList.add(0, event.dynamicBean!!)
                        recyclerView?.adapter?.notifyItemInserted(0)
                        recyclerView?.smoothScrollToPosition(0)
                    }
                }
            }

            DynamicEvent.DYNAMIC_REMOVE -> {
                var position = 0
                for ((index, bean) in mDynamicList.withIndex()) {
                    if (bean.mediaKey == event.dynamicBean?.mediaKey) {
                        position = index
                        mDynamicList.remove(bean)
                        break
                    }
                }
                recyclerView?.adapter?.notifyItemRemoved(position)
                if (mDynamicList.isEmpty()) {
                    showEmpty()
                }
            }

            DynamicEvent.DYNAMIC_UPDATE -> {
                if (event.dynamicBean != null && mDynamicList.isNotEmpty()) {
                    var position = -1
                    for (i in mDynamicList.indices) {
                        val it = mDynamicList[i]
                        if (it.mediaKey == event.dynamicBean!!.mediaKey) {
                            position = i
                            it.copy(event.dynamicBean!!)
                            break
                        }
                    }
                    if (position != -1) {
                        recyclerView?.adapter?.notifyItemChanged(position)
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onZanChange(event: VideoActionResultEvent) {
        when (event.type) {
            1 -> {
                //关注
                for (data in mDynamicList) {
                    if (data.uid.toString() == event.id) {
                        data.focus = event.action == VideoActionResultEvent.ACTION_ADD
                        break
                    }
                }
            }

            2 -> {
                //点赞
                if (event.isCommentLike) {
                    return
                }
                for (i in mDynamicList.indices) {
                    val it = mDynamicList[i]
                    if (it.mediaKey.toString() == event.id) {
                        it.likeCount = event.count
                        it.like = event.selected
                        recyclerView?.adapter?.notifyItemChanged(i)
                        break
                    }
                }
            }

            4 -> {
                if (event.actionType != VideoActionResultEvent.TYPE_DYNAMIC) {
                    return
                }
                //收藏
                for (data in mDynamicList) {
                    if (data.mediaKey == event.id) {
                        data.isCollected = event.selected
                    }
                }
            }

            else -> {}
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommentChange(event: CommentEvent) {
        var position = -1
        for (i in mDynamicList.indices) {
            val it = mDynamicList[i]
            if (it.mediaKey == event.mediaKey) {
                position = i
                it.comments = it.comments + 1
                break
            }
        }
        if (position != -1) {
            recyclerView?.adapter?.notifyItemChanged(position)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBlackListChange(event: BlackListEvent) {
        for (i in mDynamicList.indices) {
            val it = mDynamicList[i]
            if (it.uid == event.uid) {
                it.isBlackList = event.status
            }
        }
    }

    override fun showEmpty() {
        if (isSafe()) {
            val view = View.inflate(requireContext(), R.layout.layout_status_empty, null)
            view.findViewById<ImageView>(R.id.image_empty).setImageDrawable(emptyImage.drawable)
            view.findViewById<TextView>(R.id.large_tip).text = emptyLargeTip.text
            view.findViewById<TextView>(R.id.little_tip).text = emptyLittleTip.text
            (recyclerView.adapter as DynamicListAdapter).setEmptyView(view)
        }
    }


    override fun showFailed(listener: View.OnClickListener) {
        if (isSafe()) {
            val view = LoadingView(requireContext())
            view.showFailed(listener)
            (recyclerView.adapter as DynamicListAdapter).setEmptyView(view)
        }
    }
}
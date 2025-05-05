package com.cqcsy.lgsp.main.hot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.StringUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.adapter.HotTabAdapter
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.MovieModuleBean
import com.cqcsy.library.base.BaseFragment
import com.cqcsy.library.event.TabClickRefreshEvent
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_hot_tab_fragment.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * 热播Tab对应的子fragment
 */
class HotTabViewFragment : BaseFragment() {
    private var dataList: MutableList<MovieModuleBean> = ArrayList()
    private var hotAdapter: BaseQuickAdapter<MovieModuleBean, BaseViewHolder>? = null
    private var categoryId = ""

    // 筛选条件 0:周榜 1:月榜 2:年榜
    private var orderType = 0
    private var isFirst = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        categoryId = arguments?.getString("categoryId") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_hot_tab_fragment, container, false)
    }

    override fun onResume() {
        super.onResume()
        if (isFirst) {
            initView()
            isFirst = false
        }
    }

    private fun initView() {
        initRefreshLayout()
        weeklyText.isSelected = true
        weeklyText.textSize = 18f
        setClick()
        val gridLayoutManager = GridLayoutManager(context, 3)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (dataList[position].itemType == 1) {
                    3
                } else {
                    1
                }
            }
        }
        recyclerView.isNestedScrollingEnabled = true
        recyclerView.layoutManager = gridLayoutManager
        hotAdapter = HotTabAdapter(dataList)
        recyclerView.adapter = hotAdapter
        getHttpList(true)
    }

    private fun setClick() {
        weeklyText.setOnClickListener {
            if (!weeklyText.isSelected) {
                orderType = 0
                weeklyText.isSelected = true
                monthlyText.isSelected = false
                yearText.isSelected = false
                weeklyText.textSize = 18f
                monthlyText.textSize = 14f
                yearText.textSize = 14f
                getHttpList(true)
            }
        }
        monthlyText.setOnClickListener {
            if (!monthlyText.isSelected) {
                orderType = 1
                weeklyText.isSelected = false
                monthlyText.isSelected = true
                yearText.isSelected = false
                weeklyText.textSize = 14f
                monthlyText.textSize = 18f
                yearText.textSize = 14f
                getHttpList(true)
            }
        }
        yearText.setOnClickListener {
            if (!yearText.isSelected) {
                orderType = 2
                weeklyText.isSelected = false
                monthlyText.isSelected = false
                yearText.isSelected = true
                weeklyText.textSize = 14f
                monthlyText.textSize = 14f
                yearText.textSize = 18f
                getHttpList(true)
            }
        }
    }

    /**
     * 初始化下拉刷新
     */
    private fun initRefreshLayout() {
        hotTabRefresh.setEnableRefresh(true)
        hotTabRefresh.setEnableLoadMore(false)
        hotTabRefresh.setOnRefreshListener {
            // 刷新数据，请求接口
            getHttpList(false)
        }
    }

    private fun getHttpList(isView: Boolean) {
        if (isView) {
            dataList.clear()
            hotAdapter?.notifyDataSetChanged()
            showProgressView()
        }
        val params = HttpParams()
        params.put("TitleID", categoryId)
        params.put("OrderType", orderType)
        HttpRequest.post(
            RequestUrls.GET_HOT_LIST,
            object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                    hotTabRefresh.setEnableRefresh(true)
                    hotTabRefresh.finishRefresh()
                    dismissProgressView()
                    val jsonArray = response?.optJSONArray("list")
                    if (jsonArray == null || jsonArray.length() == 0) {
                        showEmptyView()
                        return
                    }
                    val jsonList: List<MovieModuleBean> = Gson().fromJson(
                        jsonArray.toString(),
                        object : TypeToken<List<MovieModuleBean>>() {}.type
                    )
                    dataList.clear()
                    for (i in jsonList.indices) {
                        if (i < 3) {
                            jsonList[i].itemType = 0
                        } else {
                            break
                        }
                    }
                    dataList.addAll(jsonList)
                    hotAdapter?.notifyDataSetChanged()
                    recyclerView.scrollToPosition(0)
                }

                override fun onError(response: String?, errorMsg: String?) {
                    if (isSafe()) {
                        hotTabRefresh.setEnableRefresh(false)
                    }
                    dataList.clear()
                    hotAdapter?.notifyDataSetChanged()
                    showFailedView { getHttpList(true) }
                }
            }, params, this
        )
    }

    private fun showProgressView() {
        if (isSafe()) {
            statusView?.showProgress()
        }
    }

    private fun dismissProgressView() {
        if (isSafe()) {
            statusView?.dismissProgress()
        }
    }

    private fun showFailedView(listener: View.OnClickListener) {
        if (isSafe()) {
            statusView?.showFailed(listener)
        }
    }

    private fun showEmptyView() {
        if (isSafe()) {
            statusView?.showEmpty()
            statusView?.findViewById<TextView>(R.id.large_tip)?.text =
                StringUtils.getString(R.string.searchNoData)
            statusView?.findViewById<TextView>(R.id.little_tip)?.text =
                StringUtils.getString(R.string.searchNoDataTips)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTabClickRefresh(event: TabClickRefreshEvent) {
        if (event.position == R.id.button_hot && mIsFragmentVisible && dataList.isNotEmpty()) {
            recyclerView.scrollToTop(hotTabRefresh)
        }
    }
}
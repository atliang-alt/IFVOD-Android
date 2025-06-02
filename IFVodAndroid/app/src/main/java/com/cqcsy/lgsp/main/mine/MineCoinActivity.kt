package com.cqcsy.lgsp.main.mine

import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.refresh.RefreshActivity
import com.cqcsy.lgsp.bean.CoinUserBean
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.utils.GlobalValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_mine_coin_header.view.*
import org.json.JSONObject
import kotlin.math.abs

/**
 * 我的金币
 */
class MineCoinActivity : RefreshActivity() {
    private var dataList: MutableList<CoinUserBean>? = null
    private var adapter: BaseQuickAdapter<CoinUserBean, BaseViewHolder>? = null
    private var headerView: View? = null

    override fun getRefreshChild(): Int {
        return R.layout.layout_recyclerview
    }

    override fun onChildAttach() {
        setHeaderTitle(R.string.mineCoin)
        emptyLargeTip.text = StringUtils.getString(R.string.noData)
        initView()
        getList(true)
    }

    override fun onRefresh() {
        super.onRefresh()
        page = 1
        getList()
    }

    override fun onLoadMore() {
        super.onLoadMore()
        getList()
    }

    private fun addHeaderLayout() {
        headerView = LayoutInflater.from(this).inflate(R.layout.layout_mine_coin_header, null)
        headerView?.coinValue?.text = GlobalValue.userInfoBean?.userExtension?.gold.toString()
    }

    private fun initView() {
        addHeaderLayout()
        dataList = ArrayList()
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter =
            object :
                BaseQuickAdapter<CoinUserBean, BaseViewHolder>(R.layout.item_coin_user, dataList) {
                override fun convert(holder: BaseViewHolder, item: CoinUserBean) {
                    holder.setText(R.id.name, item.name)
                    holder.setText(
                        R.id.date,
                        TimeUtils.date2String(
                            item.addtime?.let { TimesUtils.formatDate(it) },
                            "yyyy-MM-dd"
                        )
                    )
                    if (item.coin > 0) {
                        holder.setText(R.id.coinValue, "+" + item.coin.toString())
                        holder.setTextColor(
                            R.id.coinValue,
                            ColorUtils.getColor(R.color.grey_4)
                        )
                    } else {
                        holder.setText(R.id.coinValue, item.coin.toString())
                        holder.setTextColor(
                            R.id.coinValue,
                            ColorUtils.getColor(R.color.orange)
                        )
                    }
                }
            }
        headerView?.let { adapter?.addHeaderView(it) }
        recyclerView.adapter = adapter
    }

    private fun getList(isShow: Boolean = false) {
        if (isShow) {
            showProgress()
        }
        val params = HttpParams()
        params.put("page", page)
        params.put("size", size)
        HttpRequest.post(RequestUrls.MINE_COIN_USER, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (isShow) {
                    dismissProgress()
                }
                val jsonArray = response?.optJSONArray("list")
                if (jsonArray == null || jsonArray.length() == 0) {
                    finishLoadMoreWithNoMoreData()
                    return
                }
                val list = Gson().fromJson<MutableList<CoinUserBean>>(
                    jsonArray.toString(),
                    object : TypeToken<List<CoinUserBean>>() {}.type
                )
                if (page == 1) {
                    dataList?.clear()
                    adapter?.removeAllHeaderView()
                    adapter?.notifyDataSetChanged()
                    headerView?.allCoin?.text = response.optString("sum")
                    headerView?.usedCoin?.text = abs(response.optInt("usesum")).toString()
                    headerView?.let { adapter?.addHeaderView(it) }
                    finishRefresh()
                }
                dataList?.addAll(list)
                adapter?.notifyDataSetChanged()
                if (list.size >= size) {
                    page += 1
                    finishLoadMore()
                } else {
                    finishLoadMoreWithNoMoreData()
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                showFailed {
                    page = 1
                    getList()
                }
            }

        }, params, this)
    }
}
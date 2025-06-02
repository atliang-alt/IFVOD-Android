package com.cqcsy.lgsp.search

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.SearchListBean
import com.cqcsy.lgsp.bean.net.HotSearchNetBean
import com.cqcsy.lgsp.database.bean.SearchKeywordBean
import com.cqcsy.lgsp.database.manger.SearchKeywordManger
import com.cqcsy.library.base.BaseActivity
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.StatusBarUtil
import com.cqcsy.library.views.SearchEditView
import com.cqcsy.library.views.TipsDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_search.*
import org.json.JSONObject

/**
 * 搜索页面
 */
class SearchActivity : BaseActivity() {
    // 历史搜索集合
    private var historyWord: MutableList<SearchKeywordBean> = ArrayList()

    // 搜索关联词集合
    private var searchList: MutableList<SearchListBean> = ArrayList()

    // 热搜集合
    private var hotList: MutableList<HotSearchNetBean> = ArrayList()

    // 搜索结果目录适配器
    private var searchListAdapter: BaseQuickAdapter<SearchListBean, BaseViewHolder>? = null

    // 热搜适配器
    private var hotAdapter: BaseQuickAdapter<HotSearchNetBean, BaseViewHolder>? = null

    // 搜索关键词
    private var keyword: String = ""

    // 记录当前显示页面是不是搜索结果页
    private var isShowResultView = false

    // 记录当前显示页面是不是搜索关联词页
    private var isShowListView = false

    // 记录当前显示页面是不是搜索热搜、历史词页
    private var isShowHotView = true

    private val videoPosition = 0
    private val userPosition = 1
    private val dynamicPosition = 2
    private val albumPosition = 3
    private val fragmentContainer = HashMap<Int, Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        StatusBarUtil.setTranslucentForImageView(this, 0, null)
//        setStatusBarMode(GlobalValue.themeType)
        statusBar.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            StatusBarUtil.getStatusBarHeight(this)
        )
        initData()
        initView()
    }

    private fun initData() {
        getHotSearch()
    }

    private fun initView() {
        setHistoryLayout()
        searchListRecycle.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        hotSearchRecycle.layoutManager = GridLayoutManager(this, 2)
        setAdapter()
        searchEdit.setCustomDeletedCallback(object : SearchEditView.CustomDeletedCallback {
            override fun onDeleted() {
                keyword = ""
                showHotLayout()
                hideKeyboard(searchEdit)
            }
        })
        searchEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.isNotEmpty() && keyword != s.toString()) {
                    keyword = s.toString()
                    getSearchList(keyword)
                }
                if (searchEdit.text.toString().trim().isEmpty()) {
                    searchList.clear()
                    searchListAdapter?.notifyDataSetChanged()
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }
        })
        searchEdit.setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
            keyword = searchEdit.text.toString().trim()
            if (actionId == EditorInfo.IME_ACTION_SEARCH && keyword.isNotEmpty()) {
                // 网络请求数据
                hideKeyboard(searchEdit)
                saveSearchKeyword(keyword)
                clearTab()
                showSearchResultLayout()
                return@OnEditorActionListener true
            }
            false
        })
    }

    /**
     * 初始化搜索结果Tab
     */
    private fun setupTab() {
        val tabList = StringUtils.getStringArray(R.array.search_tab_text)
        viewPager.adapter = object : FragmentStatePagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getCount(): Int {
                return tabList.size
            }

            override fun getItem(position: Int): Fragment {
                var fragment = fragmentContainer[position]
                if (fragment == null) {
                    fragment = createFragment(position)
                    fragmentContainer[position] = fragment
                }
                return fragment
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return tabList[position]
            }
        }
        tabLayout.setupWithViewPager(viewPager)
        viewPager.offscreenPageLimit = tabList.size
    }

    private fun clearTab() {
        viewPager.adapter = null
        fragmentContainer.clear()
        tabLayout.removeAllTabs()
    }

    private fun createFragment(position: Int): Fragment {
        val bundle = Bundle()
        bundle.putSerializable("keyword", keyword)
        val fragment: Fragment = when(position) {
            videoPosition -> SearchVideoFragment()
            userPosition -> SearchUserFragment()
            dynamicPosition -> SearchDynamicFragment()
            albumPosition -> SearchAlbumFragment()
            else -> Fragment()
        }
        fragment.arguments = bundle
        return fragment
    }

    /**
     * 历史搜索
     */
    private fun setHistoryLayout() {
        historyWord = SearchKeywordManger.instance.select()
        if (historyWord.size == 0) {
            historyLayoutTitle.visibility = View.GONE
            historyWordLayout.visibility = View.GONE
            clearHistoryWordLayout.visibility = View.GONE
            return
        }
        addHistoryLayout()
    }

    /**
     * 动态添加历史搜索数据View
     */
    private fun addHistoryLayout() {
        setExpandText()
        historyLayoutTitle.visibility = View.VISIBLE
        historyWordLayout.visibility = View.VISIBLE
        clearHistoryWordLayout.visibility = View.VISIBLE
        if (historyWord.size <= 10) {
            openHistoryWord.visibility = View.GONE
        } else {
            openHistoryWord.visibility = View.VISIBLE
        }
        historyWordLayout.removeAllViews()
        val lp = MarginLayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.rightMargin = SizeUtils.dp2px(10f)
        lp.topMargin = SizeUtils.dp2px(10f)
        val isOpen = openHistoryWord.isChecked
        for ((i, bean) in historyWord.withIndex()) {
            if (isOpen) {
                //展开后显示20条
                if (i > 19) {
                    return
                }
            } else {
                //初始显示10条
                if (i > 9) {
                    return
                }
            }


            var word = bean.keyword
            if (word.length > 4) {
                word = String.format("%s...", word.substring(0, 4))
            }
            val view = TextView(this)
            view.textSize = 12f
            view.text = word
            view.setTextColor(ColorUtils.getColor(R.color.grey))
            view.setBackgroundResource(R.drawable.background_grey_corner_2)
            view.setPadding(
                SizeUtils.dp2px(10f),
                SizeUtils.dp2px(10f),
                SizeUtils.dp2px(10f),
                SizeUtils.dp2px(10f)
            )
            view.setOnClickListener {
                keyword = bean.keyword
                searchEdit.setText(keyword)
                searchEdit.setSelection(keyword.length)
                saveSearchKeyword(keyword)
                showSearchResultLayout()
                hideKeyboard(searchEdit)
            }
            historyWordLayout.addView(view, lp)
        }
    }

    private fun setAdapter() {
        searchListAdapter = object : BaseQuickAdapter<SearchListBean, BaseViewHolder>(
            R.layout.item_search_list_recycle,
            searchList
        ) {
            override fun convert(holder: BaseViewHolder, item: SearchListBean) {
                holder.setText(R.id.itemSearchListText, item.name)
                holder.getView<LinearLayout>(R.id.itemSearchListLayout)
                    .setOnClickListener { v: View? ->
                        keyword = item.name
                        showSearchResultLayout()
                        saveSearchKeyword(keyword)
                        searchEdit.setText(keyword)
                        searchEdit.setSelection(keyword.length)
                    }
            }
        }
        hotAdapter = object : BaseQuickAdapter<HotSearchNetBean, BaseViewHolder>(
            R.layout.item_search_hot_recycle,
            hotList
        ) {
            override fun convert(holder: BaseViewHolder, item: HotSearchNetBean) {
                val position = getItemPosition(item)
                holder.setText(R.id.searchHotText, item.title)
                holder.setText(R.id.searchHotNumb, (position + 1).toString())
                if (position == 0) {
                    holder.setTextColorRes(R.id.searchHotNumb, R.color.red)
                }
                if (position == 1) {
                    holder.setTextColorRes(R.id.searchHotNumb, R.color.orange)
                }
                if (position == 2) {
                    holder.setTextColorRes(R.id.searchHotNumb, R.color.yellow_2)
                }
                holder.getView<LinearLayout>(R.id.searchHotLayout)
                    .setOnClickListener { v: View? ->
                        hideKeyboard(searchEdit)
                        keyword = item.title
                        searchEdit.setText(keyword)
                        searchEdit.setSelection(keyword.length)
                        saveSearchKeyword(keyword)
                        showSearchResultLayout()
                    }
            }
        }

        searchListRecycle.adapter = searchListAdapter
        hotSearchRecycle.adapter = hotAdapter
    }

    /**
     * 获取热搜数据接口
     */
    private fun getHotSearch() {
        HttpRequest.post(RequestUrls.HOT_SEARCH, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val jsonArray = response?.optJSONArray("list")
                if (jsonArray == null || jsonArray.length() == 0) {
                    return
                }
                val list: List<HotSearchNetBean> = Gson().fromJson(
                    jsonArray.toString(),
                    object : TypeToken<List<HotSearchNetBean>>() {}.type
                )
                hotList.addAll(list)
                hotSearchTitle.visibility = View.VISIBLE
                hotAdapter?.notifyDataSetChanged()
            }

            override fun onError(response: String?, errorMsg: String?) {
                Log.e("SearchActivity：", "热搜接口请求数据失败")
            }
        }, tag = this)
    }

    /**
     * 获取关联词数据接口
     */
    private fun getSearchList(word: String) {
        OkGo.getInstance().cancelTag(RequestUrls.RELATED_WORDS)
        showSearchListLayout()
        val params = HttpParams()
        params.put("keyword", word)
        HttpRequest.post(RequestUrls.RELATED_WORDS, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val jsonArray = response?.optJSONArray("list")
                if (jsonArray == null || jsonArray.length() == 0) {
                    return
                }
                val list: List<SearchListBean> = Gson().fromJson(
                    jsonArray.toString(),
                    object : TypeToken<List<SearchListBean>>() {}.type
                )
                searchList.clear()
                searchList.addAll(list)
                searchListAdapter?.notifyDataSetChanged()
            }

            override fun onError(response: String?, errorMsg: String?) {
            }
        }, params, RequestUrls.RELATED_WORDS)
    }

    /**
     * 显示搜索结果布局，隐藏其他布局
     */
    private fun showSearchResultLayout() {
        historyAndHotLayout.visibility = View.GONE
        searchListRecycle.visibility = View.GONE
        searchResultLayout.visibility = View.VISIBLE
        setupTab()
        isShowResultView = true
    }

    /**
     * 显示搜索列表布局，隐藏其他布局
     */
    private fun showSearchListLayout() {
        searchResultLayout.visibility = View.GONE
        historyAndHotLayout.visibility = View.GONE
        searchListRecycle.visibility = View.VISIBLE
        clearTab()
        isShowListView = true
    }

    /**
     * 显示搜索热搜布局，隐藏其他布局
     */
    private fun showHotLayout() {
        searchResultLayout.visibility = View.GONE
        historyAndHotLayout.visibility = View.VISIBLE
        searchListRecycle.visibility = View.GONE
        clearTab()
        isShowHotView = true
        isShowListView = false
        isShowResultView = false
    }

    override fun onBackPressed() {
        goBack()
    }

    /**
     * 返回逻辑处理
     */
    private fun goBack() {
        if (isShowResultView) {
            isShowResultView = false
            if (isShowListView) {
                showSearchListLayout()
                return
            }
            if (isShowHotView) {
                showHotLayout()
                searchEdit.setText("")
                keyword = ""
                return
            }
        }
        if (isShowListView) {
            showHotLayout()
            searchEdit.setText("")
            keyword = ""
            isShowListView = false
            return
        }
        finish()
    }

    /**
     * 保存搜索词
     */
    private fun saveSearchKeyword(keyword: String) {
        val searchKeywordBean = SearchKeywordBean()
        searchKeywordBean.keyword = keyword
        searchKeywordBean.time = System.currentTimeMillis().toString()
        searchKeywordBean.uid = ""
        SearchKeywordManger.instance.add(searchKeywordBean)
        historyWord.clear()
        historyWord.addAll(SearchKeywordManger.instance.select())
        addHistoryLayout()
    }

    /**
     * 点击搜索
     */
    fun searchText(view: View) {
        keyword = searchEdit.text.toString().trim()
        if (!TextUtils.isEmpty(keyword)) {
            clearTab()
            saveSearchKeyword(keyword)
            showSearchResultLayout()
            hideKeyboard(searchEdit)
        }
    }

    /**
     * 清空历史搜索
     */
    fun clearHistoryWord(view: View) {
        val tipsDialog = TipsDialog(this)
        tipsDialog.setDialogTitle(R.string.clear_all)
        tipsDialog.setMsg(R.string.clear_tips)
        tipsDialog.setLeftListener(R.string.save, View.OnClickListener {
            tipsDialog.dismiss()
        })
        tipsDialog.setRightListener(R.string.clear, View.OnClickListener {
            SearchKeywordManger.instance.delete()
            historyWord.clear()
            historyWordLayout.removeAllViews()
            historyLayoutTitle.visibility = View.GONE
            historyWordLayout.visibility = View.GONE
            clearHistoryWordLayout.visibility = View.GONE
            tipsDialog.dismiss()
        })
        tipsDialog.show()
    }

    /**
     * 隐藏软键盘
     */
    private fun hideKeyboard(view: View) {
        val manager: InputMethodManager = view.context
            .getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * 展开历史搜索
     */
    fun openHistoryWord(view: View) {
        openHistoryWord.isChecked = !openHistoryWord.isChecked
        addHistoryLayout()
    }

    private fun setExpandText() {
        if (openHistoryWord.isChecked) {
            openHistoryWord.text = "收起"
        } else {
            openHistoryWord.text = "展开"
        }
    }

    /**
     * 点击标题返回按钮
     */
    fun clickBack(view: View) {
        goBack()
    }
}
package com.cqcsy.lgsp.record

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.TimeUtils
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.refresh.RefreshDataFragment
import com.cqcsy.lgsp.bean.ShortVideoBean
import com.cqcsy.lgsp.database.manger.WatchRecordManger
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.lgsp.video.VideoBaseActivity
import com.cqcsy.lgsp.video.VideoPlayVerticalActivity
import com.cqcsy.library.utils.GlobalValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.littlejerk.rvdivider.builder.XLinearBuilder
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_login_tip.*
import org.json.JSONArray

/**
 * 短视频观看记录
 */
class ShortVideoFragment : RefreshDataFragment<ShortVideoBean>() {
    var selectedItem: MutableList<ShortVideoBean> = ArrayList()
    var listener: RecordListener? = null
    var isEdit = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (GlobalValue.isLogin()) {
            getRecyclerView().setPadding(0, SizeUtils.dp2px(20f), 0, 0)
        }
    }

    override fun initData() {
        super.initData()
        emptyLargeTip.setText(R.string.commentEmpty)
        if (!GlobalValue.isLogin()) {
            getLocalData()
        }
    }

    private fun getLocalData() {
        val list = WatchRecordManger.instance.selectAllData(0)
        if (list.isNotEmpty()) {
            for (i in list.indices) {
                if (list[i].videoType == Constant.VIDEO_SHORT) {
                    val shortVideoBean = ShortVideoBean()
                    shortVideoBean.mediaKey = list[i].mediaKey
                    shortVideoBean.videoType = list[i].videoType
                    shortVideoBean.mediaUrl = list[i].mediaUrl
                    shortVideoBean.coverImgUrl = list[i].coverImgUrl
                    shortVideoBean.title = list[i].title
                    shortVideoBean.episodeId = list[i].episodeId
                    shortVideoBean.uniqueID = list[i].uniqueID
                    shortVideoBean.episodeTitle = list[i].episodeTitle
                    shortVideoBean.upperName = list[i].upperName
                    shortVideoBean.watchingProgress = list[i].watchTime.toLong()
                    shortVideoBean.duration = list[i].duration
                    shortVideoBean.date = list[i].recordTime
                    shortVideoBean.cidMapper = list[i].cidMapper
                    shortVideoBean.regional = list[i].regional
                    shortVideoBean.lang = list[i].lang
                    getDataList().add(shortVideoBean)
                }
            }
            refreshView()
        } else {
            showEmpty()
        }
        onLoadFinish()
    }

    fun setRecordListener(listener: RecordListener) {
        this.listener = listener
    }

    fun refreshList(isEdit: Boolean) {
        this.isEdit = isEdit
        if (isEdit || !GlobalValue.isLogin()) {
            disableRefresh()
        } else {
            enableRefresh()
        }
        selectedItem.clear()
        refreshView()
    }

    fun hideHeader() {
        if (loginTipContent != null) {
            loginTipContent.visibility = View.GONE
        }
        enableRefresh()
        onRefresh()
    }

    override fun onLoadFinish() {
        listener?.onLoadFinish(1, getDataList())
    }

    override fun onDataEmpty() {
        listener?.onDataEmpty(1)
    }

    override fun addPinHeaderLayout(): View? {
        if (!GlobalValue.isLogin()) {
            return View.inflate(requireContext(), R.layout.layout_login_tip, null)
        }
        return null
    }

    override fun getUrl(): String {
        return RequestUrls.GET_RECORD
    }

    override fun addDecoration(recyclerView: RecyclerView) {
        recyclerView.addItemDecoration(XLinearBuilder(recyclerView.context).setSpacing(10f).build())
    }

    override fun getParams(): HttpParams {
        val params = HttpParams()
        params.put("isNormalVideo", 0)
        return params
    }

    override fun getItemLayout(): Int {
        return R.layout.layout_short_video_record_item
    }

    override fun parsingData(jsonArray: JSONArray): MutableList<ShortVideoBean> {
        return Gson().fromJson(
            jsonArray.toString(),
            object : TypeToken<List<ShortVideoBean>>() {}.type
        )
    }

    override fun setItemView(holder: BaseViewHolder, item: ShortVideoBean, position: Int) {
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
        if (item.isUnAvailable) {
            holder.getView<ImageView>(R.id.image_short_video)
                .setImageResource(R.mipmap.icon_invalid_short_video)
            holder.setText(R.id.short_video_time, "")
        } else {
            item.coverImgUrl?.let {
                ImageUtil.loadImage(
                    requireContext(),
                    it,
                    holder.getView(R.id.image_short_video)
                )
            }
            holder.setText(R.id.short_video_time, item.duration)
        }
        holder.setText(R.id.short_video_name, item.title)
        holder.setText(
            R.id.short_video_watch_time,
            TimeUtils.date2String(TimesUtils.formatDate(item.date), "yyyy-MM-dd HH:mm")
        )
        holder.setText(R.id.upload_name, item.upperName)
    }

    override fun onItemClick(position: Int, dataBean: ShortVideoBean, holder: BaseViewHolder) {
        if (isEdit) {
            if (selectedItem.contains(dataBean)) {
                selectedItem.remove(dataBean)
            } else {
                selectedItem.add(dataBean)
            }
            holder.getView<CheckBox>(R.id.item_check).isChecked =
                selectedItem.contains(dataBean)
            return
        }
        if (!GlobalValue.isLogin()) {
            listener?.checkAvailable(1, dataBean)
        } else {
            if (dataBean.isUnAvailable) {
                selectedItem.add(dataBean)
                listener?.removeData(selectedItem)
            } else {
                startPlay(dataBean)
            }
        }
    }

    fun startPlay(dataBean: ShortVideoBean) {
        val intent = Intent(context, VideoPlayVerticalActivity::class.java)
        intent.putExtra(VideoBaseActivity.PLAY_VIDEO_BEAN, dataBean)
        startActivity(intent)
    }

    fun <T> resetList(isClear: Boolean, list: MutableList<T>?) {
        if (isClear) {
            clearAll()
        } else {
            removeData(list)
        }
        selectedItem.clear()
    }

    override fun isHttpTag(): Boolean {
        return GlobalValue.isLogin()
    }

    override fun onLogin() {
        hideHeader()
    }

    override fun isEnableClickLoading(): Boolean {
        return GlobalValue.isLogin()
    }
}
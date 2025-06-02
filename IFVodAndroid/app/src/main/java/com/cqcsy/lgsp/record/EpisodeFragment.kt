package com.cqcsy.lgsp.record

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.TimeUtils
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.refresh.RefreshDataFragment
import com.cqcsy.lgsp.bean.MovieModuleBean
import com.cqcsy.lgsp.database.manger.WatchRecordManger
import com.cqcsy.lgsp.event.AddRecordSuccess
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.event.RecordClearEvent
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
import com.shuyu.gsyvideoplayer.utils.CommonUtil
import kotlinx.android.synthetic.main.layout_login_tip.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray

/**
 * 剧集观看记录
 */
class EpisodeFragment : RefreshDataFragment<MovieModuleBean>() {
    var selectedItem: MutableList<MovieModuleBean> = ArrayList()
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
                if (list[i].videoType != Constant.VIDEO_SHORT) {
                    val movieModuleBean = MovieModuleBean()
                    movieModuleBean.mediaKey = list[i].mediaKey
                    movieModuleBean.videoType = list[i].videoType
                    movieModuleBean.mediaUrl = list[i].mediaUrl
                    movieModuleBean.coverImgUrl = list[i].coverImgUrl
                    movieModuleBean.title = list[i].title
                    movieModuleBean.episodeId = list[i].episodeId
                    movieModuleBean.uniqueID = list[i].uniqueID
                    movieModuleBean.episodeTitle = list[i].episodeTitle
                    movieModuleBean.upperName = list[i].upperName
                    movieModuleBean.watchingProgress = list[i].watchTime.toLong()
                    movieModuleBean.duration = list[i].duration
                    movieModuleBean.date = list[i].recordTime
                    movieModuleBean.cidMapper = list[i].cidMapper
                    movieModuleBean.regional = list[i].regional
                    movieModuleBean.lang = list[i].lang
                    getDataList().add(movieModuleBean)
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

    override fun onLoadFinish() {
        listener?.onLoadFinish(0, getDataList())
    }

    override fun onDataEmpty() {
        listener?.onDataEmpty(0)
    }

    fun hideHeader() {
        if (loginTipContent != null) {
            loginTipContent.visibility = View.GONE
        }
        enableRefresh()
        onRefresh()
    }

    override fun addPinHeaderLayout(): View? {
        if (!GlobalValue.isLogin()) {
            return View.inflate(requireContext(), R.layout.layout_login_tip, null)
        }
        return null
    }

    override fun addDecoration(recyclerView: RecyclerView) {
        recyclerView.addItemDecoration(XLinearBuilder(recyclerView.context).setSpacing(10f).build())
    }

    override fun getUrl(): String {
        return RequestUrls.GET_RECORD
    }

    override fun getParams(): HttpParams {
        val params = HttpParams()
        params.put("isNormalVideo", 1)
        return params
    }

    override fun getItemLayout(): Int {
        return R.layout.layout_record_film_item
    }

    override fun parsingData(jsonArray: JSONArray): MutableList<MovieModuleBean> {
        return Gson().fromJson(
            jsonArray.toString(),
            object : TypeToken<List<MovieModuleBean>>() {}.type
        )
    }

    override fun setItemView(holder: BaseViewHolder, item: MovieModuleBean, position: Int) {
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
            holder.getView<ImageView>(R.id.image_film).setImageResource(R.mipmap.icon_invalid_movie)
        } else {
            item.coverImgUrl?.let { ImageUtil.loadImage(this, it, holder.getView(R.id.image_film)) }
        }
        holder.setText(R.id.film_name, item.title)
        val itemUpdate = holder.getView<TextView>(R.id.item_update)
        val episodeName = holder.getView<TextView>(R.id.film_episode)
        val watchProgress = holder.getView<ProgressBar>(R.id.watch_progress)
        val contentType = holder.getView<TextView>(R.id.content_type)

        if (item.updateStatus.isEmpty() || item.updateCount == 0) {
            itemUpdate.visibility = View.GONE
        } else {
            itemUpdate.visibility = View.VISIBLE
            if (item.updateCount > 99) {
                itemUpdate.text = "..."
            } else {
                itemUpdate.text = item.updateCount.toString()
            }
        }
        val stringBuffer = StringBuffer()
        if (!item.cidMapper.isNullOrEmpty()) {
            stringBuffer.append(item.cidMapper?.replace(",", "·"))
        }
        if (!item.regional.isNullOrEmpty()) {
            stringBuffer.append("·" + item.regional)
        }
        if (!item.lang.isNullOrEmpty()) {
            stringBuffer.append("·" + item.lang)
        }
        if (stringBuffer.isEmpty()) {
            contentType.visibility = View.GONE
        } else {
            contentType.visibility = View.VISIBLE
            contentType.text = stringBuffer.toString()
        }
        val watchTime = holder.getView<TextView>(R.id.film_progress_time)
        if (item.episodeTitle.isNullOrEmpty() || item.videoType == Constant.VIDEO_MOVIE) {
            episodeName.visibility = View.GONE
        } else {
            episodeName.visibility = View.VISIBLE
            episodeName.text = if (item.videoType == Constant.VIDEO_TELEPLAY) {
                getString(R.string.watch_progress_episode, item.episodeTitle)
            } else getString(R.string.watch_progress, item.episodeTitle)
        }
        if (GlobalValue.isLogin()) {
            val total = TimesUtils.formatTime(item.duration)
            watchProgress.visibility = View.VISIBLE
            watchTime.visibility = View.VISIBLE
            watchProgress.progress = if (total != 0) {
                (item.watchingProgress * 100 / total).toInt()
            } else {
                0
            }
            if ((item.epSecond > 0 && item.epSecond <= item.watchingProgress) || ((item.epSecond <= 0 && total > 0 && total <= item.watchingProgress))) {
                watchTime.setText(R.string.watch_finish)
            } else {
                watchTime.text = getString(
                    R.string.watch_progress_time,
                    CommonUtil.stringForTime(item.watchingProgress * 1000L)
                )
            }
        } else {
            watchProgress.visibility = View.GONE
            watchTime.visibility = View.GONE
        }
        holder.setText(
            R.id.film_watch_time,
            TimeUtils.date2String(TimesUtils.formatDate(item.date), "yyyy-MM-dd HH:mm")
        )
    }

    override fun onItemClick(position: Int, dataBean: MovieModuleBean, holder: BaseViewHolder) {
        if (isEdit) {
            if (selectedItem.contains(dataBean)) {
                selectedItem.remove(dataBean)
            } else {
                selectedItem.add(dataBean)
            }
            holder.getView<CheckBox>(R.id.item_check).isChecked = selectedItem.contains(dataBean)
            return
        }
        if (!GlobalValue.isLogin()) {
            listener?.checkAvailable(0, dataBean)
        } else {
            if (dataBean.isUnAvailable) {
                selectedItem.add(dataBean)
                listener?.removeData(selectedItem)
            } else {
                startPlay(dataBean)
            }
        }
    }

    fun startPlay(dataBean: MovieModuleBean) {
        val intent = Intent(context, VideoPlayVerticalActivity::class.java)
        intent.putExtra(VideoBaseActivity.PLAY_VIDEO_BEAN, dataBean)
        startActivity(intent)
    }

    fun <T> resetList(isClear: Boolean, list: MutableList<T>?) {
        if (isClear) {
            clearAll()
            EventBus.getDefault().post(RecordClearEvent(RecordClearEvent.TYPE_EPISODE))
        } else {
            removeData(list)
            if (getDataList().isEmpty()) {
                EventBus.getDefault().post(RecordClearEvent(RecordClearEvent.TYPE_EPISODE))
            }
        }
        selectedItem.clear()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshEvent(event: AddRecordSuccess) {
        onRefresh()
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
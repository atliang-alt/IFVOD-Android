package com.cqcsy.lgsp.main.mine

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.TimeUtils
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.MovieModuleBean
import com.cqcsy.lgsp.event.AddRecordSuccess
import com.cqcsy.lgsp.event.VideoActionResultEvent
import com.cqcsy.lgsp.record.RecordListener
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.lgsp.video.VideoBaseActivity
import com.cqcsy.lgsp.video.VideoPlayVerticalActivity
import com.cqcsy.library.base.refresh.RefreshDataFragment
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.ImageUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.littlejerk.rvdivider.builder.XLinearBuilder
import com.lzy.okgo.model.HttpParams
import com.shuyu.gsyvideoplayer.utils.CommonUtil
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray

/**
 * 剧集收藏列表
 */
class EpisodeFragment : RefreshDataFragment<MovieModuleBean>() {
    var selectedItem: MutableList<String> = ArrayList()

    var isEdit = false

    // 1收藏时间倒序,2收藏时间正序,3更新时间倒叙,4更新时间正序
    private var sortType = 2

    var listener: RecordListener? = null

    fun setRecordListener(listener: RecordListener) {
        this.listener = listener
    }

    fun refreshList(isEdit: Boolean) {
        this.isEdit = isEdit
        if (isEdit) {
            disableRefresh()
        } else {
            enableRefresh()
        }
        selectedItem.clear()
        refreshView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emptyLargeTip.setText(R.string.no_collect)
        emptyLittleTip.setText(R.string.no_collect_tip)
    }

    override fun addPinHeaderLayout(): View? {
        val header = View.inflate(requireContext(), R.layout.layout_collect_sort_header, null)
        header.layoutParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(44f))
        val addTimeSort = header.findViewById<TextView>(R.id.addTimeSort)
        val updateTimeSort = header.findViewById<TextView>(R.id.updateTimeSort)
        val arrow = if (sortType == 1) {
            R.mipmap.icon_order_up
        } else {
            R.mipmap.icon_order_down
        }
        addTimeSort.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            arrow,
            0
        )
        addTimeSort.setOnClickListener {
            if (isEdit) {
                return@setOnClickListener
            }
            updateTimeSort.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.mipmap.icon_order_normal,
                0
            )
            updateTimeSort.setTextColor(ColorUtils.getColor(R.color.grey))
            addTimeSort.setTextColor(ColorUtils.getColor(R.color.word_color_2))
            if (sortType == 1) {
                sortType = 2
                addTimeSort.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.mipmap.icon_order_down,
                    0
                )
            } else {
                sortType = 1
                addTimeSort.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.mipmap.icon_order_up,
                    0
                )
            }
            onRefresh()
        }
        updateTimeSort.setOnClickListener {
            if (isEdit) {
                return@setOnClickListener
            }
            addTimeSort.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.icon_order_normal, 0)
            addTimeSort.setTextColor(ColorUtils.getColor(R.color.grey))
            updateTimeSort.setTextColor(ColorUtils.getColor(R.color.word_color_2))
            if (sortType == 3) {
                sortType = 4
                updateTimeSort.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.mipmap.icon_order_up,
                    0
                )
            } else {
                sortType = 3
                updateTimeSort.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.mipmap.icon_order_down,
                    0
                )
            }
            onRefresh()
        }
        return header
    }

    override fun addDecoration(recyclerView: RecyclerView) {
        recyclerView.addItemDecoration(XLinearBuilder(recyclerView.context).setSpacing(10f).build())
    }

    override fun getUrl(): String {
        return RequestUrls.EPISODE_VIDEO_COLLECTION
    }

    override fun getParams(): HttpParams {
        val params = HttpParams()
        params.put("OrderType", sortType)
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

    override fun onLoadFinish() {
        listener?.onLoadFinish(0, getDataList())
    }

    override fun onDataEmpty() {
        listener?.onDataEmpty(0)
    }

    override fun setItemView(holder: BaseViewHolder, item: MovieModuleBean, position: Int) {
        val checkBox = holder.getView<CheckBox>(R.id.item_check)
        if (isEdit) {
            checkBox.visibility = View.VISIBLE
        } else {
            checkBox.visibility = View.GONE
        }
        checkBox.isChecked = selectedItem.contains(item.mediaKey)
        checkBox.setOnClickListener {
            if (selectedItem.contains(item.mediaKey)) {
                selectedItem.remove(item.mediaKey)
            } else {
                selectedItem.add(item.mediaKey)
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
        val watchProgressState = holder.getView<TextView>(R.id.film_progress_time)
        val addTime = holder.getView<TextView>(R.id.film_watch_time)
        val total = formatTime(item.duration)

        addTime.text = TimeUtils.date2String(TimesUtils.formatDate(item.collectionTime), "yyyy-MM-dd HH:mm")

        watchProgress.visibility = View.VISIBLE
        watchProgressState.visibility = View.VISIBLE
        watchProgress.progress = if (total != 0) {
            (item.watchingProgress * 100 / total).toInt()
        } else {
            0
        }
        if ((item.epSecond > 0 && item.epSecond <= item.watchingProgress) || ((item.epSecond <= 0 && total > 0 && total <= item.watchingProgress))) {
            watchProgressState.setText(R.string.watch_finish)
        } else {
            watchProgressState.text = getString(
                R.string.watch_progress_time,
                CommonUtil.stringForTime(item.watchingProgress * 1000L)
            )
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
        if (item.videoType == Constant.VIDEO_MOVIE) {
            episodeName.visibility = View.GONE
            itemUpdate.visibility = View.GONE
//            watchTime.visibility = View.GONE
        } else {
            if (item.updateCount <= 0) {
                itemUpdate.visibility = View.GONE
            } else {
                itemUpdate.visibility = View.VISIBLE
                if (item.updateCount > 99) {
                    itemUpdate.text = "..."
                } else {
                    itemUpdate.text = item.updateCount.toString()
                }
            }
            episodeName.visibility = View.VISIBLE
            episodeName.text = item.updateStatus
//            if (item.episodeTitle.isNullOrEmpty()) {
//                watchTime.visibility = View.GONE
//            } else {
//                watchTime.visibility = View.VISIBLE
//                watchTime.text = if (item.videoType == Constant.VIDEO_TELEPLAY) {
//                    getString(R.string.watch_progress_episode, item.episodeTitle)
//                } else getString(R.string.watch_progress, item.episodeTitle)
//            }
        }

    }

    private fun formatTime(time: String): Int {
        val array = time.split(":")
        return when (array.size) {
            3 -> {
                array[0].toInt() * 60 * 60 + array[1].toInt() * 60 + array[2].toInt()
            }
            2 -> {
                array[0].toInt() * 60 + array[1].toInt()
            }
            1 -> {
                array[0].toInt()
            }
            else -> {
                0
            }
        }
    }

    override fun onItemClick(position: Int, dataBean: MovieModuleBean, holder: BaseViewHolder) {
        if (isEdit) {
            if (selectedItem.contains(dataBean.mediaKey)) {
                selectedItem.remove(dataBean.mediaKey)
            } else {
                selectedItem.add(dataBean.mediaKey)
            }
            holder.getView<CheckBox>(R.id.item_check).isChecked =
                selectedItem.contains(dataBean.mediaKey)
            return
        }
        if (dataBean.isUnAvailable) {
            selectedItem.add(dataBean.mediaKey)
            listener?.removeData(selectedItem)
            return
        }
        val intent = Intent(context, VideoPlayVerticalActivity::class.java)
        intent.putExtra(VideoBaseActivity.PLAY_VIDEO_BEAN, dataBean)
        startActivity(intent)
    }

    fun resetList(isClear: Boolean, list: MutableList<String>?) {
        if (isClear) {
            clearAll()
        } else {
            if (list != null) {
                val dataList = getDataList()
                dataList.removeAll {
                    list.contains(it.mediaKey)
                }
                refreshView()
                if (dataList.isEmpty()) {
                    showEmpty()
                    onDataEmpty()
                }
            }
        }
        selectedItem.clear()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCollectChange(event: VideoActionResultEvent) {
        if (event.type != 4 && event.actionType != VideoActionResultEvent.TYPE_EPISODE) {
            return
        }
        if (event.selected) {
            onRefresh()
        } else {
            val removeList = ArrayList<MovieModuleBean>()
            getDataList().forEach {
                if (event.id == it.mediaKey) {
                    removeList.add(it)
                }
            }
            removeData(removeList)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshEvent(event: AddRecordSuccess) {
        onRefresh()
    }
}
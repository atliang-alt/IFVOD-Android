package com.cqcsy.lgsp.main.mine

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
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.event.VideoActionResultEvent
import com.cqcsy.lgsp.record.RecordListener
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.lgsp.video.VideoBaseActivity
import com.cqcsy.lgsp.video.VideoPlayVerticalActivity
import com.cqcsy.library.utils.GlobalValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.littlejerk.rvdivider.builder.XLinearBuilder
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray

/**
 * 我的小视频收藏
 */
class ShortVideoFragment : RefreshDataFragment<ShortVideoBean>() {
    var selectedItem: MutableList<ShortVideoBean> = ArrayList()

    var isEdit = false

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
        if (GlobalValue.isLogin()) {
            getRecyclerView().setPadding(0, SizeUtils.dp2px(20f), 0, 0)
        }
    }

    override fun getUrl(): String {
        return RequestUrls.SHORT_VIDEO_COLLECTION
    }

    override fun addDecoration(recyclerView: RecyclerView) {
        recyclerView.addItemDecoration(XLinearBuilder(recyclerView.context).setSpacing(10f).build())
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

    override fun onLoadFinish() {
        listener?.onLoadFinish(1, getDataList())
    }

    override fun onDataEmpty() {
        listener?.onDataEmpty(1)
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
        holder.setText(R.id.short_video_watch_time, TimeUtils.date2String(TimesUtils.formatDate(item.date), "yyyy-MM-dd"))
        holder.setText(R.id.upload_name, item.upperName)
    }

    override fun onItemClick(position: Int, dataBean: ShortVideoBean, holder: BaseViewHolder) {
        if (isEdit) {
            if (selectedItem.contains(dataBean)) {
                selectedItem.remove(dataBean)
            } else {
                selectedItem.add(dataBean)
            }
            holder.getView<CheckBox>(R.id.item_check).isChecked = selectedItem.contains(dataBean)
            return
        }
        if (dataBean.isUnAvailable) {
            selectedItem.add(dataBean)
            listener?.removeData(selectedItem)
            return
        }
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCollectChange(event: VideoActionResultEvent) {
        if (event.type != 4 && event.actionType != VideoActionResultEvent.TYPE_SHORT) {
            return
        }
        if (event.selected) {
            onRefresh()
        } else {
            val removeList = ArrayList<ShortVideoBean>()
            getDataList().forEach {
                if (event.id == it.mediaKey) {
                    removeList.add(it)
                }
            }
            removeData(removeList)
        }
    }
}
package com.cqcsy.lgsp.record

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.*
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.refresh.RefreshDataFragment
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.database.manger.DynamicRecordManger
import com.cqcsy.lgsp.event.VideoActionResultEvent
import com.cqcsy.lgsp.upper.UpperActivity
import com.cqcsy.lgsp.upper.pictures.PictureListActivity
import com.cqcsy.lgsp.upper.pictures.PicturesBean
import com.cqcsy.lgsp.upper.pictures.UpperPicturesFragment
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.lgsp.vip.util.VipGradeImageUtil
import com.cqcsy.library.utils.GlobalValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.littlejerk.rvdivider.builder.XLinearBuilder
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_login_tip.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray

/**
 * 相册记录
 */
class AlbumRecordFragment : RefreshDataFragment<PicturesBean>() {
    var selectedItem: MutableList<PicturesBean> = ArrayList()
    var listener: RecordListener? = null
    var isEdit = false
    var isRecord: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isRecord = arguments?.getBoolean("isRecord") ?: true
        if (isRecord) {
            emptyLargeTip.setText(R.string.commentEmpty)
        } else {
            emptyLargeTip.setText(R.string.no_collect)
            emptyLittleTip.setText(R.string.no_collect_tip)
        }
        super.onViewCreated(view, savedInstanceState)
        if (!GlobalValue.isLogin()) {
            getLocalData()
        }
    }

    override fun getUrl(): String {
        return if (isRecord) RequestUrls.GET_RECORD else RequestUrls.MINE_ALBUM_COLLECT
    }

    override fun getParams(): HttpParams {
        val params = HttpParams()
        if (isRecord) {
            params.put("isNormalVideo", "2")
        }
        return params
    }

    override fun getItemLayout(): Int {
        return R.layout.item_search_album
    }

    override fun parsingData(jsonArray: JSONArray): MutableList<PicturesBean> {
        return Gson().fromJson(
            jsonArray.toString(),
            object : TypeToken<MutableList<PicturesBean>>() {}.type
        )
    }

    override fun setItemView(holder: BaseViewHolder, item: PicturesBean, position: Int) {
        val checkBox = holder.getView<CheckBox>(R.id.itemCheck)
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
        ImageUtil.loadCircleImage(this, item.headImg, holder.getView(R.id.user_image))
        setVipLevel(holder.getView(R.id.userVip), item)
        holder.setText(R.id.user_nick_name, item.upperName)
        holder.setText(
            R.id.release_time,
            StringUtils.getString(
                R.string.release_time, TimeUtils.date2String(
                    TimesUtils.formatDate(item.createTime), "yyyy-MM-dd"
                )
            )
        )
        if (item.isUnAvailable) {
            holder.getView<ImageView>(R.id.picture_cover)
                .setImageResource(R.mipmap.icon_un_available)
        } else {
            ImageUtil.loadImage(
                this,
                item.coverPath,
                holder.getView(R.id.picture_cover),
                defaultImage = R.mipmap.pictures_cover_default
            )
        }
        holder.setText(R.id.picture_size, item.photoCount.toString())
        holder.setText(R.id.picture_name, item.title)
        if (item.description.isNullOrEmpty()) {
            holder.setGone(R.id.picture_des, true)
        } else {
            holder.setVisible(R.id.picture_des, true)
            holder.setText(R.id.picture_des, item.description)
        }
        holder.setText(R.id.comment_count, NormalUtil.formatPlayCount(item.comments))
        holder.setText(R.id.zan_count, NormalUtil.formatPlayCount(item.likeCount))
        holder.getView<ImageView>(R.id.user_image).setOnClickListener {
            val intent = Intent(context, UpperActivity::class.java)
            intent.putExtra(UpperActivity.UPPER_ID, item.uid)
            startActivity(intent)
        }
    }

    private fun setVipLevel(imageView: ImageView, item: PicturesBean) {
        if (item.bigV || item.vipLevel > 0) {
            imageView.visibility = View.VISIBLE
            imageView.setImageResource(
                if (item.bigV) R.mipmap.icon_big_v else VipGradeImageUtil.getVipImage(
                    item.vipLevel
                )
            )
        } else {
            imageView.visibility = View.GONE
        }
    }

    private fun getLocalData() {
        val list = DynamicRecordManger.instance.selectAllData(1)
        if (list.isNotEmpty()) {
            for (it in list) {
                val picturesBean = PicturesBean()
//                picturesBean.id = it.pid
                picturesBean.mediaKey = it.mediaKey
                picturesBean.headImg = it.headImg
                picturesBean.upperName = it.upperName
                picturesBean.createTime = it.createTime
                picturesBean.title = it.title
                picturesBean.description = it.description
                picturesBean.coverPath = it.coverPath
                picturesBean.photoCount = it.photoCount
                picturesBean.comments = it.comments
                picturesBean.likeCount = it.likeCount
                picturesBean.uid = it.uid
                picturesBean.bigV = it.bigV
                picturesBean.vipLevel = it.vipLevel
                getDataList().add(picturesBean)
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

    fun <T> resetList(isClear: Boolean, list: MutableList<T>?) {
        if (isClear) {
            clearAll()
        } else {
            removeData(list)
        }
        selectedItem.clear()
    }

    override fun onLoadFinish() {
        listener?.onLoadFinish(3, getDataList())
    }

    override fun onDataEmpty() {
        listener?.onDataEmpty(3)
    }

    private fun hideHeader() {
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

    override fun onItemClick(position: Int, dataBean: PicturesBean, holder: BaseViewHolder) {
        if (isEdit) {
            if (selectedItem.contains(dataBean)) {
                selectedItem.remove(dataBean)
            } else {
                selectedItem.add(dataBean)
            }
            holder.getView<CheckBox>(R.id.itemCheck).isChecked =
                selectedItem.contains(dataBean)
            return
        }
        if (!GlobalValue.isLogin()) {
            listener?.checkAvailable(3, dataBean)
        } else {
            if (dataBean.isUnAvailable) {
                selectedItem.add(dataBean)
                listener?.removeData(selectedItem)
            } else {
                startIntent(dataBean)
            }
        }
    }

    fun startIntent(dataBean: PicturesBean) {
        val intent = Intent(context, PictureListActivity::class.java)
        intent.putExtra(UpperPicturesFragment.PICTURES_PID, dataBean.mediaKey)
        intent.putExtra(UpperPicturesFragment.PICTURES_TITLE, dataBean.title)
        startActivity(intent)
    }

    override fun isHttpTag(): Boolean {
        return GlobalValue.isLogin()
    }

    override fun onLogin() {
        hideHeader()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCollectChange(event: VideoActionResultEvent) {
        if (event.type != 4 || isRecord || event.actionType != VideoActionResultEvent.TYPE_PICTURE) {
            return
        }
        when (event.action) {
            VideoActionResultEvent.ACTION_ADD -> onRefresh()
            VideoActionResultEvent.ACTION_REMOVE -> {
                val removeList = ArrayList<PicturesBean>()
                getDataList().forEach {
                    if (event.id == it.mediaKey) {
                        removeList.add(it)
                    }
                }
                removeData(removeList)
            }
        }
    }

    override fun isEnableClickLoading(): Boolean {
        return GlobalValue.isLogin()
    }
}
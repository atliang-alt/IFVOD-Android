package com.cqcsy.lgsp.video.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.*
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.views.BottomBaseDialog
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.GlobalValue
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_vertical_video_bullet_input.*
import org.json.JSONObject

/**
 * 全屏竖屏 发布弹幕
 */
class DanamaInputVerticalDialog(context: Context) : BottomBaseDialog(context) {
    val MAX_DUMAKU = 25
    var listener: DanamaInputDialog.OnSendDanamaListener? = null
    var selectColor: Int? = null
    var itemList: ArrayList<ColorItem> = ArrayList()

    // 0 所有 1 昵称和头像 2 国家 3 全部关闭
    var prefix = 3

    fun setOnSendDanamaListener(listener: DanamaInputDialog.OnSendDanamaListener) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_vertical_video_bullet_input)

        initColor()
        initEdit()
        setColor()
        if (GlobalValue.isVipUser()) {
            headerNick.isEnabled = false
            location.isEnabled = false
            getBarrageConfig()
        }
        danamaSetting.setOnClickListener {
            if (positionContent.visibility == View.VISIBLE) {
                danamaSetting.isSelected = false
                positionContent.visibility = View.GONE
                showBulletInput()
            } else {
                danamaSetting.isSelected = true
                KeyboardUtils.hideSoftInput(inputEdit)
                positionContent.visibility = View.VISIBLE
            }
        }
        sendBullet.setOnClickListener {
            val input = inputEdit.text.toString()
            if (input.isNotEmpty() && listener != null) {
                listener?.onSend(input)
            }
            dismiss()
        }

        headerNick.setOnClickListener {
            if (!GlobalValue.isVipUser()) {
                listener?.onOpenVip()
                return@setOnClickListener
            }
            val type: Int = when (prefix) {
                0 -> {
                    2
                }

                1 -> {
                    3
                }

                2 -> {
                    0
                }

                else -> {
                    1
                }
            }
            putBarrageConfig(type)
        }
        location.setOnClickListener {
            if (!GlobalValue.isVipUser()) {
                listener?.onOpenVip()
                return@setOnClickListener
            }
            val type: Int = when (prefix) {
                0 -> {
                    1
                }

                1 -> {
                    0
                }

                2 -> {
                    3
                }

                else -> {
                    2
                }
            }
            putBarrageConfig(type)
        }
        setOnShowListener {
            Handler().postDelayed({
                showBulletInput()
            }, 300)
        }
        val tag = SPUtils.getInstance().getInt(Constant.KEY_SEND_DANAMA_POSITION + GlobalValue.userInfoBean?.id, 0)
        when (tag) {
            0 -> {
                rollImg.isSelected = true
                rollText.isSelected = true
                topImg.isSelected = false
                topText.isSelected = false
                bottomImg.isSelected = false
                bottomText.isSelected = false
            }

            1 -> {
                rollImg.isSelected = false
                rollText.isSelected = false
                topImg.isSelected = true
                topText.isSelected = true
                bottomImg.isSelected = false
                bottomText.isSelected = false
            }

            2 -> {
                rollImg.isSelected = false
                rollText.isSelected = false
                topImg.isSelected = false
                topText.isSelected = false
                bottomImg.isSelected = true
                bottomText.isSelected = true
            }

            else -> {
                rollImg.isSelected = true
                rollText.isSelected = true
                topImg.isSelected = false
                topText.isSelected = false
                bottomImg.isSelected = false
                bottomText.isSelected = false
            }
        }
        positionRoll.setOnClickListener {
            rollImg.isSelected = true
            rollText.isSelected = true
            topImg.isSelected = false
            topText.isSelected = false
            bottomImg.isSelected = false
            bottomText.isSelected = false
            if (positionRoll.tag != null) {
                SPUtils.getInstance().put(
                    Constant.KEY_SEND_DANAMA_POSITION + GlobalValue.userInfoBean?.id,
                    Integer.parseInt(positionRoll.tag.toString())
                )
            }
        }
        positionTop.setOnClickListener {
            if (!GlobalValue.isVipUser()) {
                listener?.onOpenVip()
                return@setOnClickListener
            }
            rollImg.isSelected = false
            rollText.isSelected = false
            topImg.isSelected = true
            topText.isSelected = true
            bottomImg.isSelected = false
            bottomText.isSelected = false
            if (positionTop.tag != null) {
                SPUtils.getInstance().put(
                    Constant.KEY_SEND_DANAMA_POSITION + GlobalValue.userInfoBean?.id,
                    Integer.parseInt(positionTop.tag.toString())
                )
            }
        }
        positionBottom.setOnClickListener {
            if (!GlobalValue.isVipUser()) {
                listener?.onOpenVip()
                return@setOnClickListener
            }
            rollImg.isSelected = false
            rollText.isSelected = false
            topImg.isSelected = false
            topText.isSelected = false
            bottomImg.isSelected = true
            bottomText.isSelected = true
            if (positionBottom.tag != null) {
                SPUtils.getInstance().put(
                    Constant.KEY_SEND_DANAMA_POSITION + GlobalValue.userInfoBean?.id,
                    Integer.parseInt(positionBottom.tag.toString())
                )
            }
        }
    }

    /**
     * 获取弹幕配置
     */
    private fun getBarrageConfig() {
        HttpRequest.post(
            RequestUrls.GET_BARRAGE_CONFIG, object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                    prefix = response?.optInt("prefix", 3) ?: 3
                    headerNick.isEnabled = true
                    location.isEnabled = true
                    when (prefix) {
                        1 -> {
                            headerNick.isChecked = true
                            location.isChecked = false
                        }

                        2 -> {
                            headerNick.isChecked = false
                            location.isChecked = true
                        }

                        else -> {
                            headerNick.isChecked = true
                            location.isChecked = true
                        }
                    }
                }

                override fun onError(response: String?, errorMsg: String?) {
                    ToastUtils.showLong(errorMsg)
                }
            }, tag = this
        )
    }

    /**
     * 设置弹幕配置
     * type 0 所有 1 昵称和头像 2 国家
     */
    private fun putBarrageConfig(type: Int) {
        headerNick.isEnabled = false
        location.isEnabled = false
        val params = HttpParams()
        params.put("prefix", type)
        HttpRequest.post(RequestUrls.PUT_BARRAGE_CONFIG, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                prefix = type
                when (type) {
                    0 -> {
                        headerNick.isChecked = true
                        location.isChecked = true
                    }

                    1 -> {
                        headerNick.isChecked = true
                        location.isChecked = false
                    }

                    2 -> {
                        headerNick.isChecked = false
                        location.isChecked = true
                    }

                    else -> {
                        headerNick.isChecked = false
                        location.isChecked = false
                    }
                }
                headerNick.isEnabled = true
                location.isEnabled = true
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
                headerNick.isEnabled = true
                location.isEnabled = true
            }
        }, params, this)
    }

    private fun initColor() {
        val itemNames = StringUtils.getStringArray(R.array.colors_item_name)
        val colors = listOf(
            Color.parseColor("#ffffff"),
            Color.parseColor("#C796CE"),
            Color.parseColor("#3BC529"),
            Color.parseColor("#00BACF"),
            Color.parseColor("#FFFC00"),
            Color.parseColor("#FE0000"),
            Color.parseColor("#FF7200"),
            Color.parseColor("#000000")
        )
        if (GlobalValue.isEnable(2)) {
            colorTips.text = StringUtils.getString(R.string.barrageVipColor)
            for (i in itemNames.indices) {
                val item = ColorItem()
                item.color = colors[i]
                item.name = itemNames[i]
                itemList.add(item)
            }
        } else {
            colorTips.text = StringUtils.getString(R.string.barrageSettingColorTip)
            val item = ColorItem()
            item.color = colors[0]
            item.name = itemNames[0]
            itemList.add(item)
        }
        val color = SPUtils.getInstance()
            .getString(Constant.KEY_SEND_DANAMA_COLOR + GlobalValue.userInfoBean?.id, "")
        selectColor = if (color.isEmpty()) {
            colors[0]
        } else {
            Color.parseColor(color)
        }
    }

    private fun initEdit() {
        inputEdit.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.isNotEmpty()) {
                    if (s.length <= MAX_DUMAKU) {
                        danmakuCounter.text = (MAX_DUMAKU - s.length).toString()
                    } else {
                        s.delete(s.length - 1, s.length)
                    }
                    sendBullet.isEnabled = true
                } else {
                    danmakuCounter.text = (MAX_DUMAKU).toString()
                    sendBullet.isEnabled = false
                }
            }
        })
        inputEdit.setOnClickListener {
            positionContent.visibility = View.GONE
            showBulletInput()
        }
        inputEdit.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                positionContent.visibility = View.GONE
                showBulletInput()
            }
        }
    }

    private fun setColor() {
        colorList.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        val adapter =
            object :
                BaseQuickAdapter<ColorItem, BaseViewHolder>(R.layout.layout_color_item, itemList) {
                override fun convert(holder: BaseViewHolder, item: ColorItem) {
                    val name = holder.getView<TextView>(R.id.colorName)
                    if (getItemPosition(item) == 0) {
                        name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    } else {
                        name.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.icon_tag_vip, 0)
                    }
                    name.text = item.name
                    val background =
                        holder.getView<FrameLayout>(R.id.colorItemRoot).background as GradientDrawable
                    background.setColor(item.color!!)
                    holder.getView<TextView>(R.id.colorName).isSelected = selectColor == item.color
                    if (selectColor == item.color) {
                        holder.getView<ImageView>(R.id.selectedImage).visibility = View.VISIBLE
                    } else {
                        holder.getView<ImageView>(R.id.selectedImage).visibility = View.GONE
                    }
                }
            }
        adapter.setOnItemClickListener { adapter, view, position ->
            val item = adapter.getItem(position) as ColorItem
            selectColor = item.color
            SPUtils.getInstance().put(
                Constant.KEY_SEND_DANAMA_COLOR + GlobalValue.userInfoBean?.id,
                ColorUtils.int2ArgbString(item.color!!)
            )
            adapter.notifyDataSetChanged()
        }
        colorList.adapter = adapter
    }

    override fun dismiss() {
        hideBulletInput()
        super.dismiss()
    }

    private fun hideBulletInput() {
        KeyboardUtils.hideSoftInput(inputEdit)
        inputEdit.setText("")
    }

    private fun showBulletInput() {
        inputEdit.requestFocus()
        KeyboardUtils.showSoftInput(inputEdit)
    }

    inner class ColorItem {
        var color: Int? = null
        var name: String? = null
    }
}
package com.cqcsy.lgsp.video.view

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.video.bean.VideoItemBean
import com.littlejerk.rvdivider.builder.XGridBuilder
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import kotlinx.android.synthetic.main.layout_video_normal_menu.*

/**
 * 剧集和普通列菜单
 */
class VideoSelectDialog(context: Context) : VideoMenuDialog(context) {
    var mListData: MutableList<VideoItemBean>? = null

    var listener: OnMenuClickListener? = null

    interface OnMenuClickListener {
        fun onItemClick(item: VideoItemBean)
        fun onOpenVipClick() {}
        fun onGoldCoinOpen(number: Int) {}
    }

    fun setMenuClickListener(listener: OnMenuClickListener) {
        this.listener = listener
    }

    fun setMenuData(data: MutableList<VideoItemBean>) {
        mListData = data
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_video_normal_menu)
        val params = scroller_view.layoutParams as LinearLayout.LayoutParams
        if (isVertical) {
            params.height = LinearLayout.LayoutParams.WRAP_CONTENT
        } else {
            params.height = LinearLayout.LayoutParams.MATCH_PARENT
        }
        scroller_view.layoutParams = params

        initList()
        menuList.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                menuList.viewTreeObserver.removeOnGlobalLayoutListener(this)
                scrollToCurrent()
            }

        })
    }

    private fun initList() {
        if (mListData == null || mListData?.size == 0) {
            return
        }
        initRecyclerView()
        setAdapter()
    }

    private fun initRecyclerView() {
        val disableManager = LinearLayoutManager(context)
        disableManager.orientation = if (isVertical) {
            val params = disableMenuList.layoutParams as LinearLayout.LayoutParams
            params.width = LinearLayout.LayoutParams.MATCH_PARENT
            params.gravity = Gravity.START
            disableMenuList.layoutParams = params
            LinearLayoutManager.HORIZONTAL
        } else {
            LinearLayoutManager.VERTICAL
        }
        disableMenuList.layoutManager = disableManager

        if (getMenuColumn() == 1) {
            val manager = LinearLayoutManager(context)
            manager.orientation = if (isVertical) {
                val params = menuList.layoutParams as LinearLayout.LayoutParams
                params.width = LinearLayout.LayoutParams.MATCH_PARENT
                params.gravity = Gravity.START
                menuList.layoutParams = params
                LinearLayoutManager.HORIZONTAL
            } else {
                LinearLayoutManager.VERTICAL
            }
            menuList.layoutManager = manager
        } else {
            if (!isVertical) {
                titleContainer.visibility = View.GONE
            }
            titleLine.visibility = View.GONE
            val manager = GridLayoutManager(context, getMenuColumn())
            menuList.layoutManager = manager
            menuList.addItemDecoration(XGridBuilder(context).setVLineSpacing(10f).setHLineSpacing(10f).setIncludeEdge(true).build())
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            menuList.layoutParams = params
        }
    }

    private fun scrollToCurrent() {
        if (!mListData!![0].isNormalMenu) {
            val playIndex = mListData!!.indexOfFirst { it.isCurrent }
            if (playIndex != -1) {
                val top = menuList.getChildAt(playIndex).top
                scroller_view.post {
                    scroller_view.scrollTo(0, top)
                }
            }
        }
    }

    private fun setAdapter() {
        val enableList = mListData?.filter { it.enbale }
        val disableList = mListData?.filter { !it.enbale }
        if (!enableList.isNullOrEmpty()) {
            enableTitle.text = enableList[0].enableTitle
            enableDesc.text = enableList[0].enableTip
            if (!enableList[0].disableTip.isNullOrEmpty()) {
                lvLockTip.text = enableList[0].disableTip
            }
            val adapter = MenuAdapter(context, R.layout.layout_video_menu_item, enableList.toMutableList(), getMenuColumn(), isVertical)

            adapter.setOnItemClickListener { _, view, position ->
                val item = adapter.getItem(position)
                if (item.text.contentEquals("4k", ignoreCase = true)) {
                    ToastUtils.showShort(R.string.watch_4k_tip)
                    return@setOnItemClickListener
                }
                if (listener != null && !(item.isCurrent && !item.isNormalMenu)) {
                    dismiss()
                    listener?.onItemClick(adapter.getItem(position))
                }
            }
            menuList.adapter = adapter
        }
        if (!disableList.isNullOrEmpty()) {
            disable_title_content.visibility = View.VISIBLE
            disableMenuList.visibility = View.VISIBLE
            open_vip.setOnClickListener {
                dismiss()
                listener?.onOpenVipClick()
            }
            if (disableList[0].goldOpenNumber > 0) {
                gold_open.visibility = View.VISIBLE
                gold_open.text = context.getString(R.string.gold_open_tip, disableList[0].goldOpenNumber)
                gold_open.setOnClickListener {
                    dismiss()
                    listener?.onGoldCoinOpen(disableList[0].goldOpenNumber)
                }
            }
            disableMenuList.adapter =
                object : BaseQuickAdapter<VideoItemBean, BaseViewHolder>(R.layout.layout_video_menu_item, disableList.toMutableList()) {
                    override fun convert(holder: BaseViewHolder, item: VideoItemBean) {
                        if (isVertical) {
                            val padding = SizeUtils.dp2px(16f)
                            holder.itemView.setPadding(padding, 0, padding, 0)
                        } else {
                            holder.itemView.setPadding(0, 0, 0, 0)
                        }
                        val name = holder.getView<TextView>(R.id.item_name)
                        name.text = item.text
                        holder.itemView.setBackgroundColor(Color.TRANSPARENT)
                        if (item.isVip) {
                            name.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.icon_vip_tag, 0)
                        } else {
                            name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                        }
                    }
                }
            enableDesc.isVisible = false
        } else {
            enableDesc.isVisible = true
        }
    }

    class MenuAdapter : BaseQuickAdapter<VideoItemBean, BaseViewHolder> {
        private var layoutParam: FrameLayout.LayoutParams? = null
        private var column = 0
        private var svgaParser: SVGAParser
        private var isVertical = false

        constructor(context: Context, layoutId: Int, data: MutableList<VideoItemBean>?, column: Int, isVertical: Boolean) : super(layoutId, data) {
            svgaParser = SVGAParser(context)
            this.column = column
            this.isVertical = isVertical
            layoutParam = if (column != 1) {
                FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(56f))
            } else {
                FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, SizeUtils.dp2px(56f))
            }
        }

        override fun convert(holder: BaseViewHolder, item: VideoItemBean) {
            if (layoutParam != null) {
                holder.itemView.layoutParams = layoutParam
            }
            if (isVertical && column == 1) {
                val padding = SizeUtils.dp2px(16f)
                holder.itemView.setPadding(padding, 0, padding, 0)
            } else {
                holder.itemView.setPadding(0, 0, 0, 0)
            }
            holder.setText(R.id.item_name, item.text)
            val name = holder.getView<TextView>(R.id.item_name)
//            name.updateLayoutParams<RelativeLayout.LayoutParams> {
//                marginStart = if (!isVertical) {
//                    addRule(RelativeLayout.CENTER_IN_PARENT, R.id.item_name)
//                    0
//                } else {
//                    addRule(RelativeLayout.ALIGN_PARENT_START, R.id.item_name)
//                    SizeUtils.dp2px(15f)
//                }
//            }
            val topImage = holder.getView<ImageView>(R.id.item_top_image)
            val bottomImage = holder.getView<SVGAImageView>(R.id.item_bottom_image)
            if (column == 1) {
                name.typeface = Typeface.DEFAULT_BOLD
            } else {
                name.typeface = Typeface.DEFAULT
            }
            if (item.isNew) {
                topImage.visibility = View.VISIBLE
            } else {
                topImage.visibility = View.GONE
            }
            if (column == 1 && item.isVip) {
                name.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.icon_vip_tag, 0)
            } else {
                name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }
            if (item.isCurrent) {
                if (item.isLive && item.isLiveError) {
                    holder.itemView.setBackgroundResource(R.drawable.red10_corner_2_bg)
                    bottomImage.visibility = View.VISIBLE
                    bottomImage.setImageResource(R.mipmap.icon_live_error_selected)
                    name.isEnabled = false
                } else if (item.isNormalMenu) {
                    name.isEnabled = true
                    holder.itemView.setBackgroundColor(Color.TRANSPARENT)
                    bottomImage.visibility = View.GONE
                    holder.itemView.isSelected = false
                    name.isSelected = true
                } else {
                    name.isEnabled = true
                    holder.itemView.setBackgroundResource(R.drawable.video_menu_item_bg_corner_2)
                    holder.itemView.isSelected = true
                    name.isSelected = true
                    bottomImage.visibility = View.VISIBLE
                    var animName = "playing_blue.svga"
                    if (item.isLive) {
                        animName = "playing_live.svga"
                    }
                    svgaParser.decodeFromAssets(animName, object : SVGAParser.ParseCompletion {
                        override fun onComplete(videoItem: SVGAVideoEntity) {
                            bottomImage.setImageDrawable(SVGADrawable(videoItem))
                            bottomImage.startAnimation()
                        }

                        override fun onError() {

                        }

                    })
                }
            } else {
                if (column == 1) {
                    holder.itemView.setBackgroundColor(Color.TRANSPARENT)
                } else {
                    holder.itemView.setBackgroundResource(R.drawable.video_menu_item_bg_corner_2)
                }
                name.isEnabled = true
                if (item.isLive && item.isLiveError) {
                    bottomImage.visibility = View.VISIBLE
                    bottomImage.setImageResource(R.mipmap.icon_live_error_normal)
                } else {
                    holder.itemView.isSelected = false
                    bottomImage.visibility = View.GONE
                }
                name.isSelected = false
            }
        }

    }
}
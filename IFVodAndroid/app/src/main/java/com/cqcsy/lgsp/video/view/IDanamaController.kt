package com.cqcsy.lgsp.video.view

/**
 * 弹幕设置回调
 */
interface IDanamaController {

    /**
     * 弹幕速度改变
     * 0=最慢  1=适中  2=最快
     */
    fun onSpeedChange(speed: Float)

    /**
     * 弹幕字号改变
     * 0=最小   1=适中  2=最大
     */
    fun onFontSizeChange(fontSize: Int)

    /**
     * 弹幕字体透明度改变
     * 透明度百分比
     */
    fun onFontBackgroundChange(transparent: Int)

    /**
     * 弹幕位置屏蔽
     * position: 0=滚动  1=顶部  2=底部
     * status:禁用状态，true 禁用；false 启用
     */
    fun onDanamaForbidden(position: Int, status: Boolean)

    /**
     * 屏蔽关键词点击
     */
    fun onForbiddenWordClick()

    /**
     * 屏蔽用户点击
     */
    fun onForbiddenUserClick()

    /**
     * 弹幕列表点击
     */
    fun onDanamaListClick()

}
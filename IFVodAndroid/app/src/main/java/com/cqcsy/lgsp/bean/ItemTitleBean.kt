package com.cqcsy.lgsp.bean

import com.cqcsy.library.base.BaseBean

/**
 ** 2022/12/6
 ** des：每一项title，是否可点击（有右侧箭头）
 **/

class ItemTitleBean(val type: Any, val itemName: String, val action: (() -> Unit?)?) : BaseBean()
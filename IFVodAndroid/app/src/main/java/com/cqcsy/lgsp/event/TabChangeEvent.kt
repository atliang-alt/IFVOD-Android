package com.cqcsy.lgsp.event

/**
 * 首页、热播tab任意跳转事件
 */
class TabChangeEvent(val parentPosition: Int, val childCategoryId: Int) {
}
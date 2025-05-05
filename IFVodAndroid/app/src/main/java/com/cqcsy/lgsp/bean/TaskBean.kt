package com.cqcsy.lgsp.bean

import com.cqcsy.library.base.BaseBean

class TaskBean: BaseBean() {
    // 任务图标
    var icon: String? = ""
    // 任务名称
    var name: String = ""
    // 已完成的任务数
    var currentValue: Int = 0
    // 任务总数
    var defaultValue: Int = 0
    // 任务金币值
    var gold: Int = 0
    // 任务经验值
    var experince: Int = 0
    // 是否已完成 0未完成 1已完成
    var status: Int = 0
    // action类型
    var actionType: String = ""
    // 用于更新任务传参值
    var userActionType: Int = 0
}
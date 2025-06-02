package com.cqcsy.lgsp.upper

import com.cqcsy.library.base.BaseBean

class UpperInfoBean : BaseBean() {
    // 关注人数
    var attentionCount: Int = 0

    // 作品总数
    var worksCount: Int = 0

    // 头像
    var avatar: String = ""

    // 背景图片
    var backgroundImage: String = ""

    // 粉丝数量
    var fansCount: Int = 0

    // 是否关注
    var focusStatus: Boolean = false

    // ID
    var id: Int = 0

    // 签名简介
    var introduce: String? = ""

    // 点赞数量
    var likeCount: Int = 0

    // 昵称
    var nickName: String = ""

    //     用户角色 0普通；1运营
    var role: Int = 0

    // VIP等级
    var vipLevel: Int = 0

    // 用户等级
    var level: Int = 0

    // 性别
    var sex: Int = -1    // 0女1男，-1没有选择

    // 总视频数量
    var totalVideo: Int = 0

    // 视频数量
    var videoCount: Int = 0

    // 小视频数量
    var smallVideoCount: Int = 0

    // 相册数量
    var photoCount: Int = 0

    // 动态数量
    var trendsCount: Int = 0

    // 用户位置信息
    var from: String? = ""

    // 是否是大V
    var bigV: Boolean = false

    // 是否被拉黑
    var isBlackList: Boolean = false

    // 账号是否冻结
    var frozenStatus: Boolean = false
}
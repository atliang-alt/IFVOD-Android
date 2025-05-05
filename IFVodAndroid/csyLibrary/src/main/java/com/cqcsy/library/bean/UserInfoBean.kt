package com.cqcsy.library.bean

import com.cqcsy.library.base.BaseBean

class UserInfoBean: BaseBean() {
    // 用户id
    var id: Int = 0
    // 用户账号
    var userNameRaw: String? = ""
    // 用户邮箱
    var email: String? = ""
    // 用户手机号
    var phone: String? = ""
    // 国家区号
    var areacode: String = ""
    // 用户昵称
    var nickName: String? = ""
    // 用户签名
    var introduce: String? = ""
    // 用户性别 -1未知 0女 1男
    var sex: Int = -1
    // 粉丝数
    var fansCount: Int = 0
    // 关注数
    var attentionCount: Int = 0

    // 账号角色 0:普通账号 1:运营账号
    var role: Int = 0

    // 开始时间
    var sDate: String = ""

    // 结束时间
    var eDate: String = ""

    // vip名称
    var vipTypeName: String? = ""

    // vip等级
    var vipLevel: Int = 0

    // vip类型；本国vip 国际vip
    var vipCategory: String? = ""

    // 收藏数量
    var collectionUpdateCount: Int = 0

    // 未读消息总数
    var totalMsgCount: Int = 0

    // token
    var token: Token = Token()

    // 用户头像
    var avatar: String? = ""

    // 用户背景
    var backgroundImage: String? = ""
    var userExtension: UserExtension? = UserExtension()
    // 邀请码
//    var inviteCode: String = ""
    // 注册是否填写邀请码
//    var isInvited: Boolean = false
    // 是否只接受互关好友私信
    var isOnlyAcceptFriend: Boolean = false
    // 是否是大V
    var bigV: Boolean = false
    // 大V有效时间
    var bigVBeginTime: String? = null
    var bigVEndTime: String? = null
    // 是否赠送vip
    var isGiveVip: Boolean = false
    // 是否弹窗
    var isEnable: Boolean = false
    // 弹框内容
    var alterDesc: String ?= ""
    // 是否已签到 0表示未签到、1表示已签到
    var clockIn: Int = 0

    // 关注的用户是否发布新动态、相册
    var newWorks: Boolean = false
    var vipCategoryId: Int = 0
    var vipCategoryName: String? = null

    fun copy(userInfoBean: UserInfoBean) {
        avatar = userInfoBean.avatar
        vipLevel = userInfoBean.vipLevel
        vipTypeName = userInfoBean.vipTypeName
        vipCategory = userInfoBean.vipCategory
        nickName = userInfoBean.nickName
        introduce = userInfoBean.introduce
        sDate = userInfoBean.sDate
        eDate = userInfoBean.eDate
        nickName = userInfoBean.nickName
        attentionCount = userInfoBean.attentionCount
        fansCount = userInfoBean.fansCount
        email = userInfoBean.email
        phone = userInfoBean.phone
        collectionUpdateCount = userInfoBean.collectionUpdateCount
        totalMsgCount = userInfoBean.totalMsgCount
        backgroundImage = userInfoBean.backgroundImage
//        isInvited = userInfoBean.isInvited
//        inviteCode = userInfoBean.inviteCode
        role = userInfoBean.role
        sex = userInfoBean.sex
        userExtension = userInfoBean.userExtension
        bigV = userInfoBean.bigV
        bigVBeginTime = userInfoBean.bigVBeginTime
        bigVEndTime = userInfoBean.bigVEndTime
        clockIn = userInfoBean.clockIn
        newWorks = userInfoBean.newWorks
        vipCategoryId = userInfoBean.vipCategoryId
        vipCategoryName = userInfoBean.vipCategoryName
    }
}
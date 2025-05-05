package com.cqcsy.library.network

import com.cqcsy.library.network.BaseUrl.H5_HOST


/**
 * H5地址
 */
object H5Address {

    // 帮助中心
    val HELP_CENTER = "$H5_HOST/help?hideNavBar=true"

    // 用户协议
    val USER_AGREEMENT = "$H5_HOST/helpDetail?id=70&hideNavBar=true"

    // 关于我们
    val ABOUT_US = "$H5_HOST/help?title=关于我们&target=%2Fsettings&hideNavBar=true"

    // VIP会员服务条款
    val VIP_AGREEMENT = "$H5_HOST/helpDetail?id=63&hideNavBar=true"

    // 上传视频协议
    val UPLOAD_VIDEO_AGREEMENT = "$H5_HOST/helpDetail?id=71&index=3&hideNavBar=true"

    // 禁止发布视频说明
    val VIDEO_FORBIDDEN_AGREEMENT = "$H5_HOST/helpDetail?id=71&index=3&hideNavBar=true"

    // 广告中心
    val ADVERT_CENTER = "$H5_HOST/ad-center"

    // 活动规则
    val INVITE_AGREEMNT = "$H5_HOST/activityRules?hideNavBar=true"
}
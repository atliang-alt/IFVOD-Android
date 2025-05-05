package com.cqcsy.library.utils

/**
 * 常量类
 */
object Constant {
    /**
     * 首页数据类型
     */
    // 广告
    const val ADVERTISEMENT_TYPE = -1

    // 正在追
    const val FOLLOWING_TYPE = -2

    // Banner
    const val BANNER_TYPE = 0

    // 热门
    const val POPULAR_TYPE = 1

    // 电影
    const val MOVIE_TYPE = 3

    // 电视剧
    const val TELEPLAY_TYPE = 4

    // 综艺
    const val VARIETY_TYPE = 5

    // 动漫
    const val COMIC_TYPE = 6

    // 纪录片
    const val DOCUMENTARY_TYPE = 7

    // 体育
    const val SPORTS_TYPE = 95

    // 为你推荐
    const val RECOMMENDED_TYPE = 9

    // 页面中筛选
    const val PAGE_FILTER = 1001

    // 默认轮播时间
    const val DELAY_TIME = 8000L

    // 获取国家区域数据间隔时间
    const val COUNTRY_TIME = 10 * 24 * 60 * 60 * 1000

    /** 所有key值管理 **/
    const val KEY_USER_INFO = "userInfo"    // 用户信息

    const val KEY_SPLASH_ADVERT = "splashAdvert"    // 启动页广告

    const val KEY_COUNTRY_AREA_INFO = "countryAreaInfo"    // 国家区域

    const val KEY_COUNTRY_AREA_INFO_TIME = "saveAreaTime"    // 保存国家区域时间

    const val KEY_NAVIGATION_BAR = "homeNavigationBar"  // 首页tab

    const val KEY_HOT_BAR = "hotNavigationBar"  // 热播tab

    const val KEY_NAVIGATION_VERSION = "versionNo"  // 首页tab版本号

    const val KEY_HOT_VERSION = "hotVersionNo"  // 热播tab版本号

    const val KEY_SEND_DANAMA_COLOR = "sendDanamaColor"  // 发送弹幕颜色

    const val KEY_SEND_DANAMA_POSITION = "sendDanamaPosition"   // 发送弹幕位置

    const val KEY_WATCH_DANAMA_SPEED = "watchDanamaSpeed"   // 观看弹幕速度   1：慢   2：适中   3：快

    const val KEY_WATCH_DANAMA_FONT = "watchDanamaFont"   // 观看弹幕字号     1：小   2：适中   3：大

    const val KEY_WATCH_DANAMA_ALPHA = "watchDanamaAlpha"   // 观看弹幕透明度

    const val KEY_WATCH_DANAMA_FORBIDDEN = "watchDanamaForbidden"   // 观看弹幕屏蔽位置

    const val KEY_SWITCH_DANAMA = "danamaSwitch"   // 弹幕开关

    const val KEY_AUTO_SKIP = "autoSkip"   // 自动跳过片头片尾

//    const val KEY_AUTO_PLAY_NEXT = "autoPlayNext"   // 自动播放下一集

//    const val KEY_AUTO_PLAY_MOBILE_NET = "autoPlayMobileNet"   // 流量下自动播放

    const val KEY_AUTO_DOWNLOAD_MOBILE_NET = "autoDownloadMobileNet"   // 流量下载

    const val KEY_AUTO_UPLOAD_MOBILE_NET = "autoUploadMobileNet"   // 流量上传

    const val KEY_PUSH_MESSAGE_STATUS = "pushMessageStatus"   // 消息通知

    const val ACTIVITY_SWITCH = "activitySwitch"   // 活动开关

    const val ACTIVITY_AREA = "activityAreas"   // 活动开起区域

    const val AREA_CODE = "areaCode"   // 设置区域代码

    const val IS_NOTCH_IN_SCREEN = "isNotchInScreen"   // 是否是刘海屏 true是 false不是

    const val KEY_FIND_NAVIGATION_VERSION = "findVideoTabVersionNo"  // 发现tab版本号

    const val KEY_FIND_NAVIGATION_TAB = "findNavigationBar"  // 发现tab

    const val KEY_NOTICE_IDS = "noticeIds"  // 所有已显示的公告id

    const val KEY_SIGN_TIP_TIMES = "signTipTimes"  // 显示签到提示的时间

    const val MAX_BACKGROUND_TIME_ADVERT = 10 * 60 * 1000    // 后台10分钟再次进入app，会显示启动页广告

    const val KEY_SHOW_AREA_SETTING = "showAreaSetting"  // 显示区域设置，显示出来即把所选择的区域应用到广告和客服

    const val KEY_DYNAMIC_ALBUM_LABELS = "dynamicAndAlbumLabels"  // 动态、相册标签

    const val KEY_BIG_V_SWITCH = "bigVSwitch"  // 大V认证开关

    const val KEY_BIG_V_URL = "bigVUrl"  // 大V认证h5地址

    const val KEY_SHORT_VIDEO_LABELS = "shortVideoLabels"  // 小视频标签

    const val KEY_FEED_BACK = "feedBackUrl"  // 反馈

    const val KEY_LAST_SHORT_UPPER_ID = "lastShortUpperId"  // 上次播放小视频UP主ID

    const val KEY_RELEASE_BASE_URL = "keyReleaseBaseUrl"    // api域名

    const val KEY_RELEASE_SOCKET_URL = "keyReleaseSocketUrl"    // socket域名

    const val KEY_RELEASE_H5_URL = "keyReleaseH5Url"    // h5域名

    const val KEY_VIP_ACTIVITY_TIME = "VipActivityTime"  // vip活动

    const val KEY_FIRST_SWITCH_VIDEO_GUIDE = "key_first_switch_video_guide"  // 视频动态详情引导显示

    const val KEY_OPEN_NOTIFICATION = "key_open_notification"  // 消息通知禁止提醒标志

    const val KEY_NOTIFICATION_TIME = "key_notification_time"  // 上次提示时间

    const val KEY_LIVE_LINE = "key_live_line"  // 直播默认播放线路

    /**
     * 简繁体转换
     */
    const val KEY_CURRENT_LANGUAGE = "currentLanguage"

    /**
     * 视频资源类型值 (视频详情页)
     * 0：电影 1：电视剧 2：综艺 3：小视频 100：直播  101：转播
     */
    const val VIDEO_MOVIE = 0
    const val VIDEO_TELEPLAY = 1
    const val VIDEO_VARIETY = 2
    const val VIDEO_SHORT = 3
    const val VIDEO_LIVE = 100
    const val VIDEO_TV = 101

    /**
     * 上传小视频后的三种状态值
     * -1:全部状态、3:已发布、2:审核中、5: 转码中、6:不通过
     */
    const val ALL_STATUS: String = "-1"
    const val UNDER_REVIEW: String = "2"
    const val RELEASING: String = "3"
    const val ENCODE: String = "5"
    const val NO_ADOPT: String = "6"

    /**
     * 登录类型
     */
    const val MOBIL_TYPE: Int = 2
    const val EMAIL_TYPE: Int = 1

    /**
     * 上传状态 0: 正在上传  1: 暂停上传 2: 等待上传 3: 上传完成 4: 上传失败
     */
    const val UPLOADING: Int = 0
    const val UPLOAD_PAUSE: Int = 1
    const val UPLOAD_WAIT: Int = 2
    const val UPLOAD_FINISH: Int = 3
    const val UPLOAD_ERROR: Int = 4
}
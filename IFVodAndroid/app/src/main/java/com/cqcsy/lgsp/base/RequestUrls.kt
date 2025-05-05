package com.cqcsy.lgsp.base

import com.cqcsy.library.network.BaseUrl.BASE_URL

/**
 * 视频接口地址
 */
object RequestUrls {
    // 首页推荐接口
    val HOME_RECOMMEND_URL = BASE_URL + "api/List/IndexPageRecomData"

    // 首页为你推荐接口
//    val GET_RECOMMENDED_YOU = BASE_URL + "api/List/GetRecommendedYou"

    // 首页换一换接口
    val HOME_CHANGE = BASE_URL + "api/Home/GetChangeBatchVideo"

    // 首页导航分类接口
    val HOME_NAVIGATION_BAR = BASE_URL + "api/List/NavigationBar"

    // 首页电影、电视剧接口
    val HOME_RELATION_VIDEOS = BASE_URL + "api/Home/GetRelativeVideos"

    // 首页小视频二级分类接口
    val HOME_SHORT_VIDEO_SECOND = BASE_URL + "api/Home/GetRelativeVideosBySub"

    // 热搜接口
    val HOT_SEARCH = BASE_URL + "api/Home/GetHotSearch"

    // 搜索关联词接口
    val RELATED_WORDS = BASE_URL + "api/Home/GetKeyWord"

    // 搜索结果接口
    val SEARCH_RESULT = BASE_URL + "api/List/GetTitleGetData"

    // 导航频道接口
    val CHANNEL_NAVIGATION = BASE_URL + "api/Home/GetChannelNavigation"

    // 二级导航分类接口
    val SECOND_NAVIGATION_BAR = BASE_URL + "api/List/GetFilterTagsData"

    // 二级导航分类获取筛选结果接口 剧集的
    val FILTER_RESULT = BASE_URL + "api/List/GetConditionFilterData"

    // 二级导航分类获取筛选结果接口 小视频的
    val SHORT_VIDEO_FILTER = BASE_URL + "api/List/GetSmallVideo?videoType=3"

    // 获取验证码接口
    val GET_VERIFICATION_CODE = BASE_URL + "api/Login/MakeCodes"

    // 登陆
    val LOGIN = BASE_URL + "api/Login/Logins"

    // 退出登录
    val LOGOUT = BASE_URL + "api/Login/Logouts"

    // 判断验证码是否正确接口
    val JUDGE_VALID_CODES = BASE_URL + "api/Login/IsValidCodes"

    // 判断图片验证码是否正确接口
    val JUDGE_IMAGE_CODES = BASE_URL + "api/Login/VerificationCode"

    // 注册接口
    val REGISTER = BASE_URL + "api/Login/Registers"

    // 验证账号是否可用接口
    val ACCOUNT_CHECK = BASE_URL + "api/Login/IsAvailableAccounts"

    // 获取图形验证码接口
    val GET_IMAGE_CODE = BASE_URL + "api/Login/SecurityCode"

    // 刷新Token接口
    val REFRESH_TOKEN = BASE_URL + "api/Login/Validates"

    // 重置密码接口
    val RESET_PASSWORD = BASE_URL + "api/Login/ResetPasswords"

    // 点赞、取消点赞接口
    val VIDEO_LIKES = BASE_URL + "api/Video/VideoLikes"

    // 不喜欢、喜欢接口
    val VIDEO_DISLIKES = BASE_URL + "api/Video/VideoDisLikes"

    // 收藏、取消收藏接口
    val VIDEO_COLLECTION = BASE_URL + "api/Video/AddToScjs"

    // 关注、取消关注接口
    val VIDEO_FOLLOW = BASE_URL + "api/Video/FocusUPMaster"

    // 发布、回复评论投票接口
    val RELEASE_COMMENT = BASE_URL + "api/Video/Replys"

    // 删除评论接口
    val DELETE_COMMENT = BASE_URL + "api/Video/DelReply"

    // 评论列表接口
    val COMMENT_LIST = BASE_URL + "api/Video/CommentListsV2"

    // 更多回复数据
    val REPLY_LIST = BASE_URL + "api/Video/GetCommentByParentID"

    // 评论点赞接口
    val COMMENT_LIKE = BASE_URL + "api/Video/ReplyLike"

    // 投票接口
    val VOTE = BASE_URL + "api/Video/VoteLike"

    // 发送弹幕接口
    val SEND_BARRAGE = BASE_URL + "api/Video/AddBarrages"

    // 获取弹幕列表接口
    val GET_BARRAGE_LIST = BASE_URL + "api/Video/GetBarrages"

    // 视频详情页接口
    val VIDEO_INFO = BASE_URL + "api/Video/VideoDetails"

    // 视频选集接口
    val VIDEO_CHOSE = BASE_URL + "api/Video/VideoChooseGather"

    // 屏蔽弹幕用户
    val FORBIDDEN_USER = BASE_URL + "api/Video/ShieldUser"

    // 获取屏蔽用户列表
    val FORBIDDEN_USER_LIST = BASE_URL + "api/Video/GetShieldUserList"

    // 移除屏蔽用户
    val REMOVE_FORBIDDEN = BASE_URL + "api/Video/RemoveShieldUser"

    // 举报弹幕
    val ACCUSATION_BARRAGE = BASE_URL + "api/Video/AccusationBarrage"

    // 弹幕点赞
    val LIKE_BARRAGE = BASE_URL + "api/Video/BarrageLike"

    // 举报评论
    val REPORT_COMMENT = BASE_URL + "api/Video/ReportUserReq"

    // 添加屏蔽关键词
    val ADD_FORBIDDEN_WORD = BASE_URL + "api/Video/ShieldKeyWord"

    // 移除屏蔽关键词
    val REMOVE_FORBIDDEN_WORD = BASE_URL + "api/Video/RemoveShieldKeyWord"

    // 获取屏蔽关键词列表
    val GET_FORBIDDEN_WORD = BASE_URL + "api/Video/GetShieldKeyWordList"

    // 获取清晰度\语言
    val SEARCH_LANG = BASE_URL + "api/Video/SearchLangResolution"

    // 获取视频播放地址
    val VIDEO_PLAY_INFO = BASE_URL + "api/Video/getPlayInfo"

    // 添加播放记录
    val ADD_RECORD = BASE_URL + "api/Video/AddPlayRecord"

    // 获取播放记录
    val GET_RECORD = BASE_URL + "api/Video/GetPlayRecordList"

    // 清空播放记录
    val CLEAR_RECORD = BASE_URL + "api/Video/RemovePlayRecord"

    // 删除播放记录
    val DELETE_RECORD = BASE_URL + "api/Video/DelPlayRecord"

    // 播放次数记录
    val PLAY_RECORD = BASE_URL + "api/Video/PlayCountUpdate"

//    // 查询所有清晰度
//    val SEARCH_ALL_RESOLUTION = BASE_URL + "api/Video/SearchAllResolution"

    // 获取VIP套餐、支付方式信息
    val VIP_TYPE_INFO = BASE_URL + "api/User/GetVipPayType"

    /**
     * 是否是马来西亚地区
     */
    val IS_MALAYSIA = BASE_URL + "api/User/IsMalaysia"

    // 获取up主信息
    val UPPER_INFO = BASE_URL + "api/User/GetUpperInfo"

    // 获取up主所有资源
    val UPPER_ALL_RESOURCE = BASE_URL + "api/user/GetUpAllTheWorks"

    // 获取up主剧集列表
    val UPPER_VIDEO_INFO = BASE_URL + "api/User/GetUpperVideoList"

    // 获取up主小视频列表
    val UPPER_SHORT_VIDEO_INFO = BASE_URL + "api/User/GetUpperSmallVideoList"

    // 发现推荐视频列表
    val FIND_RECOMMEND_VIDEO = BASE_URL + "api/User/GetRecommendSmallVideoList"

    // 发现关注视频列表
    val FIND_ATTENTION_VIDEO = BASE_URL + "api/User/GetUserAttentionSmallVideoList"

    // 关注用户列表
    val FIND_ATTENTION_USER = BASE_URL + "api/User/GetUserAttentionList"

    // 粉丝用户列表
    val FANS_USER = BASE_URL + "api/User/GetUserFansList"

    // 发现推荐用户列表
    val FIND_RECOMMEND_USER = BASE_URL + "api/User/GetRecommendList"

    // 获取屏蔽用户状态
    val CHAT_FORBIDDEN_STATUS = BASE_URL + "api/User/PreventUserStatus"

    // 黑名单(屏蔽用户私信、评论)添加、删除
    val CHAT_FORBIDDEN = BASE_URL + "api/User/PreventUser"

    // 黑名单列表
    val GET_BLACK_TABLE = BASE_URL + "api/User/GetBlackList"

    // 清空聊天记录
    val CLEAR_CHAT_MESSAGE = BASE_URL + "api/User/ClearMessage"

    // 聊天记录
    val CHAT_MESSAGE_RECORD = BASE_URL + "api/User/MessageList"

    // 设置聊天记录为已读
    val CHAT_READ_STATUS = BASE_URL + "api/user/UpdateMessageReadStatus"

    // 发送消息
    val CHAT_MESSAGE_SEND = BASE_URL + "api/User/SendMessage"

//    // 轮询消息
//    val CHAT_MESSAGE_ROLL = BASE_URL + "api/User/MessagePollList"

    // 热播导航栏
    val GET_HOT_BAR = BASE_URL + "api/User/HotPlayBar"

    // 热播列表
    val GET_HOT_LIST = BASE_URL + "api/User/GetHotPlayVideoList"

    // 用户信息
    val USER_INFO = BASE_URL + "api/User/GetUserInfo"

    // 更新用户信息
    val UPDATE_USER_INFO = BASE_URL + "api/User/UpdateUserInfo"

    // 上传文件
    val UPLOAD_FILE = BASE_URL + "api/User/UploadFile"

    // 上传文件
    val UPLOAD_FILE_NOT_LOGIN = BASE_URL + "api/User/UploadFileNoLogin"

    // 我收藏的小视频
    val SHORT_VIDEO_COLLECTION = BASE_URL + "api/User/GetMyScjSmallVideoList?videoType=3"

    // 我收藏的剧集
    val EPISODE_VIDEO_COLLECTION = BASE_URL + "api/User/GetMyScjVideoList"

    // 按编号删除收藏
    val DELETE_COLLECTION = BASE_URL + "api/User/DeleteScjVideo"

    // 清空收藏
    val CLEAR_COLLECTION = BASE_URL + "api/User/DeleteAllScjVideo"

    // 获取消息未读数量
    val GET_MESSAGE_COUNT = BASE_URL + "api/User/GetMessageCount"

    // 获取未读消息列表
    val GET_MESSAGE_LIST = BASE_URL + "api/User/MessageNotReadListV2"

    // 我发起的投票
    val MINE_RELEASE_VOTE = BASE_URL + "api/User/MyPutVote"

    // 我参与的投票
    val MINE_JOIN_VOTE = BASE_URL + "api/User/MyJoinVote"

    // 检查更新
    val CHECK_VERSION = BASE_URL + "api/User/CheckVersion"

    // 在线反馈
    val ONLINE_SUGGESTION = BASE_URL + "api/User/AddSuggestion"

    // 订单
    val ORDERS = BASE_URL + "api/User/VipRecordList"

    // 验证密码
    val VALIDATE_PASSWORD = BASE_URL + "api/Login/ValidatePwd"

    // 更改手机号、邮箱
    val CHANGE_ACCOUNT = BASE_URL + "api/Login/UpdateAccounts"

    // 获取正在追列表
    val FOLLOWING_LIST = BASE_URL + "api/List/UserRunning"

    // 获取上传视频列表
    val GET_UPLOAD_VIDEO = BASE_URL + "api/Video/GetMyUploadSmallVideoList"

    // 获取上传服务器地址
    val GET_UPLOAD_SERVICE = BASE_URL + "api/User/GetPublishPoint"

    // 创建上传文件
    val CREATE_UPLOAD_FILE = BASE_URL + "api/User/CreateVideoFile"

    // 保存上传文件
    val SAVE_UPLOAD_FILE = BASE_URL + "api/User/SaveVideo"

    // 重新编辑后保存上传信息
    val RESUBMIT_UPLOAD_INFO = BASE_URL + "api/User/ReSubmitVideo"

    // 获取上传信息
    val GET_UPLOAD_INFO = BASE_URL + "api/Video/UploadVideoInfo"

    // 设置视频为热播
    val SET_VIDEO_HOT = BASE_URL + "api/Video/UpdateVideoIsHot"

    // 获取上传信息
    val DELETE_UPLOAD_INFO = BASE_URL + "api/Video/DeleteUploadVideo"

    // 获取广告
    val GET_ADS = BASE_URL + "api/Video/GetAdsData"

    // 推送tag绑定
//    val PUSH_TAG_BIND = BASE_URL + "api/Login/UpdateTag"

    // 获取下载地址
    val GET_DOWNLOAD_LINK = BASE_URL + "api/Video/GetDownloadLink"

    // 求片
    val VIDEO_WANTED = BASE_URL + "api/Video/ReqAndOpinion"

    // 补填邀请码
    val WRITE_INVITE_CODE = BASE_URL + "api/user/BindUserInviteCode"

    // 邀请列表
    val INVITE_LIST = BASE_URL + "api/user/GetMyInviteList"

    // 活动规则
    val INVITE_RULE = BASE_URL + "api/User/getActivityRule"

    // 获取城市区域和当前区域
    val COUNTRY_LIST = BASE_URL + "api/Login/countrylist"

    // 添加分享次数
    val ADD_SHARE_COUNT = BASE_URL + "api/Video/Share"

    // 获取下载地址
    val APP_DOWNLOAD = BASE_URL + "api/Video/AppDownload"

    // 金币播放视频
    val PAY_VIDEO_BY_GOLD = BASE_URL + "api/video/PayVideoByGold"

    // 屏蔽广告统计以及广告数量查询
//    val AD_CONTROL = BASE_URL + "api/Video/GetAdsFilter"
    val AD_CONTROL = "https://ppt.lgsp.tv/filter/a"

    // 签到情况
    val GET_BONUS = BASE_URL + "api/Home/GetBonus"

    // 提交签到
    val REQ_BONUS = BASE_URL + "api/Home/ReqBonus"

    // 获取完成任务列表
    val GET_ALL_TASK = BASE_URL + "api/Home/GetAllTask"

    // 更新任务数据
    val UPD_TASK = BASE_URL + "api/Home/UpdTask"

    // 我的金币使用数据
    val MINE_COIN_USER = BASE_URL + "api/User/GetMyCoinList"

    // 金币解锁视频
    val GOLD_PAY_VIDEO = BASE_URL + "api/video/PayVideoByGold"

    // 金币跳过广告
    val COIN_SKIP_AD = BASE_URL + "api/video/FilterVideoByGold"

    // 通过IP获取定位
    val GET_USER_REGION = BASE_URL + "api/home/GetUserRegion"

    // 投诉
    val COMPLAINT = BASE_URL + "api/user/ComplainReq"

    // 只接收互关好友私信开关
    val SWITCH_FRIEND_MESSAGE = BASE_URL + "api/user/SwitchForOnlyFriend"

    /**相册相关**/
    // 获取up主相册
    val UPPER_PICTURES = BASE_URL + "api/Album/GetUPListAlbum"

    // 获取我的相册
    val MINE_PICTURES = BASE_URL + "api/Album/MyListAlbum"

    // 创建相册
    val CREATE_PICTURES = BASE_URL + "api/Album/CreateAlbum"

    // 获取相册详情
    val ALBUM_DETAILS = BASE_URL + "api/Album/AlbumDetails"

    // 编辑相册
    val EDIT_ALBUM = BASE_URL + "api/Album/EditAlbum"

    // 删除相册
    val DELETE_ALBUM = BASE_URL + "api/Album/DelAlbum"

    // 上传相册图片
    val UPLOAD_ALBUM_PHOTO = BASE_URL + "api/Album/UploadFile"

    // 保存上传成功后的相册图片
    val INSERT_ALBUM_PHOTO = BASE_URL + "api/Album/InsertPhoto"

    // 相册热度
    val PICTURES_HOT = BASE_URL + "api/Album/Hot"

    // 相册图片转移
    val MOVE_PICTURES = BASE_URL + "api/Album/MovePhoto"

    // 相册图片删除
    val DELETE_PICTURES = BASE_URL + "api/Album/DelPhoto"

    // 相册排序设置
    val SET_SORT = BASE_URL + "api/Album/SetAlbumSort"

    // 根据类型获取小视频列表
    val VIDEO_LIST_BY_TYPE = BASE_URL + "api/user/GetSmallVideoList"

    // 发现推荐接口
    val FIND_RECOMMEND = BASE_URL + "api/user/GetSmallVideoListV2"

    // 获取小视频tab分类
    val VIDEO_TYPE = BASE_URL + "api/list/SmallNavigationBar"

    // 设置弹幕配置
    val PUT_BARRAGE_CONFIG = BASE_URL + "api/user/PutBarrageCofig"

    // 获取弹幕配置
    val GET_BARRAGE_CONFIG = BASE_URL + "api/user/getBarrageCofig"

    //搜索查询用户
    val SEARCH_USER = BASE_URL + "api/user/GetUpByNickName"

    //搜索查询动态
    val SEARCH_DYNAMIC = BASE_URL + "api/trends/SearchTrendsList"

    //搜索查询相册
    val SEARCH_ALBUM = BASE_URL + "api/album/SearchPhotoList"

    //发表动态
    val RELEASE_DYNAMIC = BASE_URL + "api/Trends/CreateTrends"

    //动态图片批量上传
    val DYNAMIC_IMAGE_UPLOAD = BASE_URL + "api/Album/BatchUploaFile"

    //获取所以动态
    val UPPER_DYNAMIC = BASE_URL + "api/Trends/UpListTrends"

    //获取动态详情
    val GET_DYNAMIC_INFO = BASE_URL + "api/Trends/GetTrends"

    // 首页为你推荐瀑布流
    val RECOMMEND_INDEX = BASE_URL + "api/list/GetRecommendForYou"

    // 小视频为你推荐瀑布流
    val SHORT_VIDEO_RECOMMEND = BASE_URL + "api/home/GetOtherSmallVideos"

    // 发现——关注的用户混合数据
//    val FOCUS_USER_RECOMMEND_LIST = BASE_URL + "api/user/GetFocusForUpData"

    // 申请大V
//    val APPLY_BIG_V = BASE_URL + "api/user/ApplyForDoubleV"

    // 获取公告
    val GET_NOTICE = BASE_URL + "api/user/GetNotice"

    // 获取指定相册信息
    val GET_ALBUM = BASE_URL + "api/Album/GetAlbum"

    // 获取T支付客服信息
    val GET_PAY_SERVICE = BASE_URL + "api/payment/GetCusService"

    // 创建H5网页支付订单
    val CREATE_WEB_PAY = BASE_URL + "api/payment/CreateAliPay"

    // 更新收藏夹更新状态
    val UPDATE_COLLECT_STATUS = BASE_URL + "api/user/UpdateCollectByID"

    // 更新关注用户作品状态
    val UPDATE_NEW_WORK_STATUS = BASE_URL + "api/user/SetReadStateByType"

    // 我的相册收藏接口
    val MINE_ALBUM_COLLECT = BASE_URL + "api/User/GetMyScjAlbumList"

    // 我的动态收藏接口
    val MINE_DYNAMIC_COLLECT = BASE_URL + "api/User/GetMyScjTrendsList"

    // 判断作品是否失效
    val CHECK_AVAILABLE = BASE_URL + "api/video/CheckWork"

    // 获取相关控制的开关
    val ACTIVITY_SWITCH = BASE_URL + "api/home/GetActivity"

    // 取消下载
    val CANCEL_DOWNLOAD = BASE_URL + "api/video/DownDiminishing"

    // 确认授权登录
    val CONFIRM_AUTH_LOGIN = BASE_URL + "api/login/ConfirmaAuth"

    // 获取动态标签
    val DYNAMIC_TAGS = BASE_URL + "api/Trends/GetLabel"

    // 获取清晰度播放地址
    val GET_PLAY_ADDRESS = BASE_URL + "api/video/GetPlayAddress"

    // 相关视频推荐
    val RECOMMEND_RELATED_VIDEO = BASE_URL + "api/list/GetRelatedVideo"

    // 获取自己的邀请码
    val INVITE_CODE = BASE_URL + "api/user/GetInviteCode"

    // 获取多个广告类型
    val ADS_LIST = BASE_URL + "api/Video/GetAdsDataList"

    // 获取静态网页地址
    val SERVER_HTML = BASE_URL + "api/home/GetAgreement"

    // 兑换会员
    val EXCHANGE_VIP = BASE_URL + "api/home/CheckCodeByAppAsync"

    // 视频评分详情
    val RATING = BASE_URL + "api/Video/VideoRatingDetails"

    // 动态小视频推荐列表
    val DYNAMIC_VIDEO_LIST = BASE_URL + "api/List/TrendsSmallVideo"

    // 获取活动信息
    val RECHARGE_ACTIVITY = BASE_URL + "api/user/GetRechargeActivity"

    // 领取弹幕广告金币
    val GET_BARRAGE_COIN = BASE_URL + "api/Video/GetBarrageCoin"

    // 获取Upper的动态小视频
    val GET_UPPER_TRENDS_VIDEO = BASE_URL + "api/Trends/UpTrendsSmallVideo"

    // 获取砍价活动状态
    val INVITE_ACTIVITY_STATUS = BASE_URL + "api/user/GetUserBargainStatus"

    // 选定套餐参加邀请砍价活动
    val START_ACTIVITY = BASE_URL + "api/Bargain/JoinActivity"

    // 放弃邀请砍价活动
    val GIVE_UP_ACTIVITY = BASE_URL + "api/Bargain/GiveUp"

    // 免费领取邀请活动VIP
    val FREE_VIP = BASE_URL + "api/Bargain/ReceiveForFree"

    // 咨询客服列表
    val SERVICE_LIST = BASE_URL + "api/home/GetCusServiceConfig"
}
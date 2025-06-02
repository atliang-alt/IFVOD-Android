package com.cqcsy.lgsp.bean.net

import com.cqcsy.lgsp.bean.VideoDetailsBean
import com.cqcsy.lgsp.bean.VideoLikeBean
import com.cqcsy.lgsp.video.bean.LanguageBean
import com.cqcsy.library.base.BaseBean
import com.cqcsy.library.bean.UserInfoBean

/**
 * 视频播放页简介页数据网络Bean
 */
class VideoIntroductionNetBean : BaseBean() {
    // 视频详情数据
    var detailInfo: VideoDetailsBean? = null

    // 视频发布者信息
    var userInfo: UserInfoBean? = null

    // 是否已关注
    var focusStatus: Boolean = false

    // 是否已拉黑
    var isBlackList: Boolean = false

    // 点赞信息
    var like: VideoLikeBean? = null

    // 不喜欢信息
    var disLike: VideoLikeBean? = null

    // 收藏信息
    var favorites: VideoLikeBean? = null

    // 相关剧集数据
//    var relatedEpisodes: MutableList<VideoRelatedEpisodesBean> = ArrayList()

    // 所有语言列表
    var languageList: MutableList<LanguageBean> = ArrayList()
}
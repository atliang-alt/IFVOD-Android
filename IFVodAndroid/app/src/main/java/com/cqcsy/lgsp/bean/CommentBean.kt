package com.cqcsy.lgsp.bean

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.cqcsy.library.base.BaseBean
import com.cqcsy.library.bean.UserInfoBean

/**
 * 评论数据Bean
 */
class CommentBean : BaseBean(), MultiItemEntity {
    // 评论ID
    var replyID: Int = 0

    // 被回复的消息ID
    var oldReplyID: Int = 0

    // 评论文本内容
    var contxt: String = ""

    var isContextExpand = false // 评论是否展开

    // 该评论用户信息
    var replierUser: UserInfoBean? = null

    // 被回复的用户信息
    var respondentUser: UserInfoBean? = null

    // 回复数量
    var repliesNumber: Int = 0

    // 点赞数
    var likesNumber: Int = 0

    // 点赞状态
    var likeStatus: Boolean = false

    // 发布时间
    var postTime: String = ""

    // 是否已删除
    var deleteState: Boolean = false

    // vip表情
    var vipexpression: String = ""

    // 投票总人数
    var participateCount: Int = 0

    // 投票类型 1：单选 2：多选
    var voteType: Int = 0

    // 投票选项集合
    var voteItem: MutableList<VoteOptionBean> = ArrayList()

    // 是否已投票
    var voteStatus: Boolean = false

    // 是否是小视频的评论数据
    var isShortData: Boolean = false

    // 是否是加入黑名单
    var isForbidden: Boolean = false

    // 数据类型，0：评论 1：投票
    override var itemType: Int = 0

    // 首次打开展开回复
    var children: MutableList<CommentBean>? = null

    // 回复评论页面
    var pageIndex = 1

    // 折叠后去掉的已加载数据，下次展开可以直接使用
    var loadedComment: MutableList<CommentBean> = ArrayList()
}
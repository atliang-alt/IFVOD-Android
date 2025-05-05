package com.cqcsy.lgsp.video.bean

import com.cqcsy.lgsp.bean.VideoBaseBean

class VideoGroupBean {
    var groupName: String? = null
    var isExpand: Boolean = false
    var itemList: MutableList<VideoBaseBean>? = null
}
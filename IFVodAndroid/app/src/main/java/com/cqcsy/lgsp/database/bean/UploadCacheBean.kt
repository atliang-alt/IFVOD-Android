package com.cqcsy.lgsp.database.bean

import com.cqcsy.library.base.BaseBean

/**
 * 上传视频缓切片后的数据
 */
class UploadCacheBean: BaseBean() {
    // 视频ID
    var lid: Int = 0
    // 生成的视频ID
    var vid: Int = 0
    // 视频切片编号
    var chunk: Int = 0
    // 视频总切片数量
    var chunks: Int = 0
    // 上传服务器地址ID
    var serviceId: Int = 0
    // 上传服务器地址URl
    var uploadUrl: String = ""
    // 创建文件生成的ID
    var fileId: String = ""
    // 上传完后服务端返回的文件名
    var fileName: String = ""
    // 标题
    var title: String = ""
    // 简介
    var context: String = ""
    // 视频原始路径
    var path: String = ""
    // 视频大小
    var videoSize: Long = 0
    // 当前下载的文件大小
    var progress: Long = 0
    // //网速，byte/s
    var speed: Long = 0
    // 视频切片开始位置
    var cutStart: Long = 0
    // 当前视频切片文件名
    var cutFileName: String = ""
    // 封面图本地路径
    var imagePath: String = ""
    // 封面图本Base64字符串
    var imageBase: String = ""
    // 视频分类id
    var cid: String = ""
    // 标签名称，逗号拼接
    var labels: String = ""
    // 视频上传状态 0: 正在上传  1: 暂停上传 2: 等待上传 3: 上传完成 4: 上传失败
    var status: Int = 0
}
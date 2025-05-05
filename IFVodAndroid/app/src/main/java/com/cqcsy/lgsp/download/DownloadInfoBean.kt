package com.cqcsy.lgsp.download

import com.lzy.okgo.model.HttpHeaders
import com.lzy.okgo.model.HttpParams
import java.io.Serializable

class DownloadInfoBean : Serializable {
    var downloadUrl: String? = null
    var headers: HttpHeaders? = null
    var params: HttpParams? = null
    var folderName: String? = null
    var fileName: String? = null
    var extra: String? = null
    var mediaKey: String = ""
    var uniqueId: String = ""
    var episodeTitle: String? = null
    var tag: String? = null
}
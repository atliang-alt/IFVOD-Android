package com.cqcsy.lgsp.offline

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.lgsp.bean.VideoBaseBean
import com.cqcsy.lgsp.download.DownloadMgr
import com.cqcsy.library.download.server.DownloadListener
import com.cqcsy.library.download.server.OkDownload
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.library.views.TipsDialog
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.lzy.okgo.model.Progress
import kotlinx.android.synthetic.main.activity_offline.*
import kotlinx.android.synthetic.main.activity_offline.editContent
import java.io.File

class OfflineActivity : NormalActivity() {
    var isEdit = false
    var editTab = -1
    var downloadTag = ""

    override fun getContainerView(): Int {
        return R.layout.activity_offline
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.download)

        setupTab()
    }

    override fun onResume() {
        super.onResume()
        setDownloadInfo()
    }

    private fun setListener() {
        val task = OkDownload.getInstance().getTask(downloadTag)
        if (task == null) {
            resetDownloading()
            return
        }
        task.unRegister(downloadTag)
        task.register(object : DownloadListener(downloadTag) {
                override fun onFinish(t: File, progress: Progress) {
                    downloadSpeed.text = getString(R.string.download_finish)
                    resetDownloading()
                    refreshFragment(downloadType.selectedTabPosition)
                    Handler().postDelayed({ setDownloadInfo() }, 1000)
                }

                override fun onRemove(progress: Progress) {

                }

                override fun onProgress(progress: Progress) {
                    downloadSpeed.setTextColor(ColorUtils.getColor(R.color.blue))
                    downloadSpeed.text = NormalUtil.formatFileSize(this@OfflineActivity, progress.speed) + "/s"
                    downloadProgress.progress = (progress.fraction * 100).toInt()
                }

                override fun onError(progress: Progress) {
                    downloadSpeed.setTextColor(ColorUtils.getColor(R.color.red))
                    downloadSpeed.text = getString(R.string.download_error)
                }

                override fun onStart(progress: Progress) {

                }
            })
    }

    private fun resetDownloading() {
        downloadTag = ""
        downloadName.text = ""
        downloadSpeed.text = ""
        downloadProgress.progress = 0
    }

    private fun refreshFragment(position: Int) {
        if (position == 0) {
            if (supportFragmentManager.findFragmentByTag("0") != null) {
                (supportFragmentManager.findFragmentByTag("0") as EpisodeOfflineFragment).refreshList(
                    isEdit
                )
            }
        } else if (position == 1) {
            if (supportFragmentManager.findFragmentByTag("1") != null) {
                (supportFragmentManager.findFragmentByTag("1") as ShortVideoOfflineFragment).refreshList(
                    isEdit
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (downloadTag.isNotEmpty())
            OkDownload.getInstance().getTask(downloadTag)?.unRegister(downloadTag)
    }

    private fun setDownloadInfo() {
        val download = DownloadMgr.getDownloading()
        if (download.isEmpty()) {
            downloadTag = ""
            downloadInfoContent.visibility = View.GONE
            return
        }
        downloadInfoContent.visibility = View.VISIBLE
        downloadNumber.text = download.size.toString()
        if (download.isEmpty()) {
            resetDownloading()
        } else if (download.size == 1) {
            val progress = download[0]
            setDownloadInfo(progress)
        } else {
            var downloadStatus = false
            for (progress in download) {
                if (progress.status == Progress.LOADING || downloadTag == progress.tag) {
                    downloadTag = progress.tag
                    setDownloadInfo(progress)
                    downloadStatus = true
                    break
                }
            }
            if (!downloadStatus) {
                downloadName.text = ""
                downloadSpeed.text = ""
                downloadProgress.progress = 0
                resetDownloading()
            }
        }
        if (downloadTag.isNotEmpty()) {
            setListener()
        }
    }

    private fun setDownloadInfo(progress: Progress) {
        downloadTag = progress.tag
        downloadProgress.progress = (progress.fraction * 100).toInt()
        when (progress.status) {
            Progress.PAUSE -> {
                downloadSpeed.setTextColor(ColorUtils.getColor(R.color.red))
                downloadSpeed.text = getString(R.string.pauseing)
            }
            Progress.ERROR -> {
                downloadSpeed.setTextColor(ColorUtils.getColor(R.color.red))
                downloadSpeed.text = getString(R.string.download_error)
            }
            Progress.LOADING -> {
                downloadSpeed.setTextColor(ColorUtils.getColor(R.color.blue))
                downloadSpeed.text = NormalUtil.formatFileSize(this, progress.speed) + "/s"
            }
            else -> {
                downloadSpeed.setTextColor(ColorUtils.getColor(R.color.grey))
                downloadSpeed.text = getString(R.string.wait_download)
            }
        }
        if (progress.extra1 != null && progress.extra1.toString().isNotEmpty()) {
            val bean = Gson().fromJson(
                progress.extra1.toString(),
                VideoBaseBean::class.java
            )
            downloadName.text = bean.title + " " + bean.episodeTitle
        }
    }

    fun setRightState(fragment: Fragment) {
        val data = if (fragment is EpisodeOfflineFragment) {
            fragment.getCount()
        } else {
            (fragment as ShortVideoOfflineFragment).getCount()
        }
        if (data == 0) {
            rightTextView.visibility = View.GONE
            if (isEdit) {
                onRightClick(rightTextView)
            }
        } else {
            rightTextView.visibility = View.VISIBLE
            if (isEdit) {
                setRightText(R.string.cancel)
            } else {
                setRightText(R.string.edit)
            }
        }
    }

    override fun onRightClick(view: View) {
        if (isEdit) {
            setRightText(R.string.edit)
            editContent.visibility = View.GONE
            leftImageView.visibility = View.VISIBLE
            downloadType.removeAllTabs()
            downloadType.setSelectedTabIndicator(R.drawable.tab_indicator)
            downloadType.addTab(downloadType.newTab().setText(R.string.episode))
            downloadType.addTab(downloadType.newTab().setText(R.string.short_video))
            if (editTab in 0..1) {
                downloadType.getTabAt(editTab)?.select()
            }
        } else {
            setRightText(R.string.cancel)
            editContent.visibility = View.VISIBLE
            leftImageView.visibility = View.INVISIBLE
            editTab = downloadType.selectedTabPosition
            downloadType.setSelectedTabIndicator(null)
            downloadType.removeAllTabs()
            if (editTab == 0) {
                downloadType.addTab(downloadType.newTab().setText(R.string.episode))
            } else {
                downloadType.addTab(downloadType.newTab().setText(R.string.short_video))
            }
        }
        isEdit = !isEdit
        if (editTab == 0) {
            (supportFragmentManager.findFragmentByTag("0") as EpisodeOfflineFragment).refreshList(
                isEdit
            )
        } else {
            (supportFragmentManager.findFragmentByTag("1") as ShortVideoOfflineFragment).refreshList(
                isEdit
            )
        }
    }

    fun onDownloadingClick(view: View) {
        val intent = Intent(this, DownloadingActivity::class.java)
        startActivity(intent)
    }

    fun clearAll(view: View) {
        val tipsDialog = TipsDialog(this)
        tipsDialog.setDialogTitle(R.string.clear_all)
        tipsDialog.setMsg(R.string.clear_tips)
        tipsDialog.setLeftListener(R.string.think_again) {
            tipsDialog.dismiss()
        }
        tipsDialog.setRightListener(R.string.clear) {
            tipsDialog.dismiss()
            deleteVideo(true)
        }
        tipsDialog.show()
    }

    fun delete(view: View) {
        val selected = if (editTab == 0) {
            (supportFragmentManager.findFragmentByTag("0") as EpisodeOfflineFragment).getSelectItem()
        } else {
            (supportFragmentManager.findFragmentByTag("1") as ShortVideoOfflineFragment).getSelectItem()
        }
        if (selected.isNullOrEmpty()) {
            ToastUtils.showLong(R.string.select_data_tip)
            return
        }
        val tipsDialog = TipsDialog(this)
        tipsDialog.setDialogTitle(R.string.delete_record)
        tipsDialog.setMsg(R.string.delete_video_tips)
        tipsDialog.setLeftListener(R.string.save) {
            tipsDialog.dismiss()
        }
        tipsDialog.setRightListener(R.string.delete) {
            tipsDialog.dismiss()
            deleteVideo(false)
        }
        tipsDialog.show()
    }

    private fun deleteVideo(removeAll: Boolean) {
        val removeList: MutableList<VideoBaseBean>? = if (removeAll) {
            if (editTab == 0) {
                (supportFragmentManager.findFragmentByTag("0") as EpisodeOfflineFragment).getAllDownloaded()
            } else {
                (supportFragmentManager.findFragmentByTag("1") as ShortVideoOfflineFragment).getAllDownloaded()
            }
        } else {
            if (editTab == 0) {
                (supportFragmentManager.findFragmentByTag("0") as EpisodeOfflineFragment).getSelectItem()
            } else {
                (supportFragmentManager.findFragmentByTag("1") as ShortVideoOfflineFragment).getSelectItem()
            }
        }
        if (removeList != null && removeList.size > 0) {
            for (bean in removeList) {
                bean.episodeKey.let { DownloadMgr.deleteTask(it, true) }
//                if (bean.videoType == Constant.VIDEO_SHORT || bean.videoType == Constant.VIDEO_MOVIE) {
//
//                } else {
//                    if(!bean.filePath.isNullOrEmpty()) {
//                        FileUtils.delete(File(bean.filePath).parent)
//                    }
//                }
            }
            if (editTab == 0) {
                (supportFragmentManager.findFragmentByTag("0") as EpisodeOfflineFragment).removeItems(
                    removeList
                )
            } else {
                (supportFragmentManager.findFragmentByTag("1") as ShortVideoOfflineFragment).removeItems(
                    removeList
                )
            }
        }
        isEdit = !removeAll
        onRightClick(rightContent)
    }

    private fun setupTab() {
        downloadType.addTab(downloadType.newTab().setText(R.string.episode))
        downloadType.addTab(downloadType.newTab().setText(R.string.short_video))
        downloadType.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (downloadType.tabCount != 2) {
                    return
                }
                val transaction = supportFragmentManager.beginTransaction()
                var fragment = supportFragmentManager.findFragmentByTag(tab?.position.toString())
                if (fragment == null) {
                    when (tab?.position) {
                        0 -> fragment = EpisodeOfflineFragment()
                        1 -> fragment = ShortVideoOfflineFragment()
                    }
                    if (fragment != null) {
                        transaction.add(R.id.fragmentContainer, fragment, tab?.position.toString())
                    }
                }
                for (temp in supportFragmentManager.fragments) {
                    transaction.hide(temp)
                }
                transaction.addToBackStack(null)
                if (fragment != null) {
                    transaction.show(fragment).commitAllowingStateLoss()
                }
                NormalUtil.clearTabLayoutTips(downloadType)
            }

        })
        val transaction = supportFragmentManager.beginTransaction()

        val fragment = EpisodeOfflineFragment()
        transaction.add(R.id.fragmentContainer, fragment, "0").show(fragment).commitAllowingStateLoss()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK || requestCode == 2000) {
            if (downloadType.selectedTabPosition == 0) {
                (supportFragmentManager.findFragmentByTag("0") as EpisodeOfflineFragment).refreshList(
                    false
                )
            } else {
                (supportFragmentManager.findFragmentByTag("1") as ShortVideoOfflineFragment).refreshList(
                    false
                )
            }
        }
    }
}
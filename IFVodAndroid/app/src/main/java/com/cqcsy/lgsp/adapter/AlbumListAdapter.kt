package com.cqcsy.lgsp.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.main.mine.AlbumDetailsActivity
import com.cqcsy.lgsp.upper.pictures.PicturesBean
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.library.uploadPicture.PictureUploadManager
import com.cqcsy.library.uploadPicture.PictureUploadStatus
import com.cqcsy.library.uploadPicture.PictureUploadTask
import com.cqcsy.library.utils.ImageUtil

/**
 * 我的相册列表适配器
 */
class AlbumListAdapter(val context: Context, val data: MutableList<PicturesBean>) :
    RecyclerView.Adapter<AlbumListAdapter.GridViewHolder>() {
    val TYPE_HEADER = 0
    val TYPE_ITEM = 1
    val TYPE_FOOTER = 2
    private var headView: View? = null
    private var footView: View? = null
    private var headViewSize = 0
    private var footViewSize = 0

    fun addHeadView(view: View) {
        headView = view
        headViewSize = 1
    }

    fun addFootView(view: View) {
        footView = view
        footViewSize = 1
    }

    override fun getItemViewType(position: Int): Int {
        var type = TYPE_ITEM
        if (headViewSize == 1 && position == 0) {
            //头部
            type = TYPE_HEADER
        } else if (footViewSize == 1 && position == itemCount - 1) {
            //最后一个位置
            type = TYPE_FOOTER
        }
        return type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val view = when (viewType) {
            TYPE_HEADER -> headView
            TYPE_FOOTER -> footView
            TYPE_ITEM -> LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_picture_item, parent, false)
            else -> null
        }
        return GridViewHolder(view ?: View(parent.context), viewType)
    }

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        if (holder.itemViewType == TYPE_ITEM) {
            val item = data[position - headViewSize]
            holder.imgView?.let {
                ImageUtil.loadImage(
                    context,
                    item.coverPath,
                    it,
                    defaultImage = R.mipmap.pictures_cover_default
                )
            }
            val task = PictureUploadManager.getTaskInfoByTag(item.mediaKey)
            val uploadCount = (task?.totalTagSize?.minus(task.finishTagSize)) ?: 0
            if (uploadCount == 0) {
                holder.uploadCount?.visibility = View.GONE
            } else {
                holder.uploadCount?.visibility = View.VISIBLE
                holder.uploadCount?.text = uploadCount.toString()
            }
            holder.name?.text = item.title
            holder.size?.text = item.photoCount.toString()
            holder.lookCount?.text = NormalUtil.formatPlayCount(item.viewCount)
            holder.zanCount?.text = NormalUtil.formatPlayCount(item.likeCount)
            holder.commentCount?.text = NormalUtil.formatPlayCount(item.comments)
            holder.itemView.setOnClickListener {
                val intent = Intent(context, AlbumDetailsActivity::class.java)
                intent.putExtra(AlbumDetailsActivity.ALBUM_ID, item.mediaKey)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size + headViewSize + footViewSize
    }

    /**
     * 上传后页面通知刷新
     */
    fun setUpload(task: PictureUploadTask) {
        for (i in data.indices) {
            if (data[i].mediaKey == task.taskTag) {
                if (task.status == PictureUploadStatus.FINISH) {
                    data[i].photoCount += 1
                }
                notifyItemChanged(i + 1)
                break
            }
        }
    }

    inner class GridViewHolder(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView) {
        var imgView: ImageView? = null
        var size: TextView? = null
        var name: TextView? = null
        var lookCount: TextView? = null
        var zanCount: TextView? = null
        var commentCount: TextView? = null
        var uploadCount: TextView? = null

        init {
            if (viewType == TYPE_ITEM) {
                imgView = itemView.findViewById(R.id.picture_cover)
                size = itemView.findViewById(R.id.picture_size)
                name = itemView.findViewById(R.id.picture_name)
                lookCount = itemView.findViewById(R.id.lookCount)
                zanCount = itemView.findViewById(R.id.zanCount)
                commentCount = itemView.findViewById(R.id.commentCount)
                uploadCount = itemView.findViewById(R.id.uploadCount)
            }
        }
    }
}
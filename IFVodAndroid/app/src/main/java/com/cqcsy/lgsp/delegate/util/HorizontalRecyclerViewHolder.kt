package com.cqcsy.lgsp.delegate.util

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.cqcsy.lgsp.R

/**
 ** 2022/12/12
 ** des：横向recyclerview holder
 **/

class HorizontalRecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
}
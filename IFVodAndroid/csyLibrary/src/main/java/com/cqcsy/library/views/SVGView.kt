package com.cqcsy.library.views

import android.content.Context
import android.util.AttributeSet
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAImageView

/**
 ** 2023/9/25
 ** des：
 **/

class SVGView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : SVGAImageView(context, attrs) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val svg = drawable as? SVGADrawable
        if (svg != null) {
            startAnimation()
        }
    }

}
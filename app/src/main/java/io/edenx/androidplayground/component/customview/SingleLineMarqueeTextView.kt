package io.edenx.androidplayground.component.customview

import android.content.Context
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.widget.Scroller
import androidx.appcompat.widget.AppCompatTextView

class SingleLineMarqueeTextView(context: Context?, attrs: AttributeSet?, defStyle: Int) :
    AppCompatTextView(context!!, attrs, defStyle) {
    private var mScroller: Scroller? = null
    private var mXPaused = 0
    var isPaused = true
        private set
    var roundDuration = 4000
        private set

    constructor(context: Context?) : this(context, null) {
        setSingleLine()
        ellipsize = null
        visibility = INVISIBLE
        isHorizontalFadingEdgeEnabled = true
    }

    constructor(context: Context?, attrs: AttributeSet?) : this(
        context,
        attrs,
        android.R.attr.textViewStyle
    ) {
        setSingleLine()
        ellipsize = null
        visibility = INVISIBLE
        isHorizontalFadingEdgeEnabled = true
    }

    fun startScroll() {
        Handler(Looper.getMainLooper()).postDelayed({
            mXPaused = -1 * width
            isPaused = true
            resumeScroll()
        }, width.toLong())
    }

    private fun resumeScroll() {
        if (!isPaused) {
            return
        }
        setHorizontallyScrolling(true)
        mScroller = Scroller(this.context, LinearInterpolator())
        setScroller(mScroller)
        val scrollingLen = calculateScrollingLen()
        val distance = scrollingLen - (width + mXPaused)
        val duration = (roundDuration * distance * 1.00000 / (text.length + width)).toInt()
        visibility = VISIBLE
        if (width == 0)
            mScroller?.forceFinished(true)
        else
            mScroller?.startScroll(mXPaused, 0, distance, 0, duration)
        invalidate()
        isPaused = false
    }

    private fun calculateScrollingLen(): Int {
        val tp = paint
        var rect: Rect? = Rect()
        val strTxt = text.toString()
        tp.getTextBounds(strTxt, 0, strTxt.length, rect)
        val scrollingLen = (rect?.width() ?: 0) + width
        rect = null
        return scrollingLen
    }

    /**
     * pause scrolling the text
     */
//    fun pauseScroll() {
//        if (null == mScroller) {
//            return
//        }
//        if (isPaused) {
//            return
//        }
//        isPaused = true
//        // abortAnimation sets the current X to be the final X,
//        // and sets isFinished to be true
//        // so current position shall be saved
//        mXPaused = mScroller!!.currX
//        mScroller!!.abortAnimation()
//    }

    override fun computeScroll() {
        super.computeScroll()
        if (null == mScroller) return
        mScroller?.let {
            if (it.isFinished && !isPaused) startScroll()
        }
    }

    fun setRndDuration(duration: Int) {
        roundDuration = duration
    }

    init {
        setSingleLine()
        ellipsize = null
        visibility = INVISIBLE
        isHorizontalFadingEdgeEnabled = true
    }
}
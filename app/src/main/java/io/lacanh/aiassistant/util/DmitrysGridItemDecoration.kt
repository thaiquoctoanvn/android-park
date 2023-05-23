package io.lacanh.aiassistant.util

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class DmitrysGridItemDecoration(
    private val gridSpacing: Int,
    private val spanCount: Int
) : RecyclerView.ItemDecoration() {

    private var mNeedLeftSpacing = false

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val padding: Int =
            parent.width / spanCount - ((parent.width - gridSpacing.toFloat() * (spanCount - 1)) / spanCount).toInt()
        val itemPosition = (view.layoutParams as RecyclerView.LayoutParams).viewAdapterPosition
        if (itemPosition < spanCount) {
            outRect.top = 0
        } else {
            outRect.top = gridSpacing
        }
        if (itemPosition % spanCount == 0) {
            outRect.left = 0
            outRect.right = padding
            mNeedLeftSpacing = true
        } else if ((itemPosition + 1) % spanCount == 0) {
            mNeedLeftSpacing = false
            outRect.right = 0
            outRect.left = padding
        } else if (mNeedLeftSpacing) {
            mNeedLeftSpacing = false
            outRect.left = gridSpacing - padding
            if ((itemPosition + 2) % spanCount == 0) {
                outRect.right = gridSpacing - padding
            } else {
                outRect.right = gridSpacing / 2
            }
        } else if ((itemPosition + 2) % spanCount == 0) {
            mNeedLeftSpacing = false
            outRect.left = gridSpacing / 2
            outRect.right = gridSpacing - padding
        } else {
            mNeedLeftSpacing = false
            outRect.left = gridSpacing / 2
            outRect.right = gridSpacing / 2
        }
        outRect.bottom = 0
    }
}
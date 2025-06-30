package io.github.crabster87.autoscrollablerecyclerview

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView

class AdaptedRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : RecyclerView(context, attrs) {

    private var lastKnownPosition: Int = -1

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        scrollToPosition(0)
    }

    fun setOnScrollListener(scrollingAction: (Int) -> Unit) {
        this.apply {
            addOnScrollListener(object : OnScrollListener() {

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == SCROLL_STATE_IDLE) {
                        val layoutManager =
                            recyclerView.layoutManager as? LinearLayoutManager ?: return
                        val position = layoutManager.findSnapPosition()
                        if (position != NO_POSITION && position != lastKnownPosition) {
                            lastKnownPosition = position
                            scrollingAction.invoke(position)
                        }
                    }
                }

            })
        }.also { LinearSnapHelper().attachToRecyclerView(it) }
    }

    private fun LinearLayoutManager.findSnapPosition(): Int {
        val snapHelper = LinearSnapHelper()
        val snapView = snapHelper.findSnapView(this) ?: return NO_POSITION
        return getPosition(snapView)
    }

}
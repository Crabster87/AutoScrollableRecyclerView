package io.github.crabster87.autoscrollablerecyclerview

import android.view.View

object ViewHelper {

    fun View.visibility(isVisible: Boolean, isGone: Boolean = true) {
        visibility = if (isVisible) {
            View.VISIBLE
        } else {
            if (isGone) {
                View.GONE
            } else {
                View.INVISIBLE
            }
        }
    }

}
package io.github.crabster87.autoscrollablerecyclerview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.LinearLayout
import android.widget.ProgressBar

class SegmentedLinearProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : LinearLayout(context, attrs) {

    private var segmentCount: Int = 0
        set(value) {
            field = value
            initializeProgressBarSegments()
            drawSegmentedProgressBar()
        }

    private var isAutoScrollable: Boolean = true

    private var progressSpacing: Int = 0
    private var progressMinValue: Int = 0
    private var progressMaxValue: Int = 0
    private var progressBackgroundColor: Int = 0
    private var progressColor: Int = 0
    private var progressCornerRadius: Int = 0
    private var progressAlpha: Float = 1f

    private class ProgressSegment(val progressBar: ProgressBar, var animator: ValueAnimator? = null)

    private val segments: MutableList<ProgressSegment> = mutableListOf()

    fun setSegmentedLinearProgressBarParams(params: ProgressBarParams) {
        params.also {
            progressSpacing = it.progressSpacing
            progressMinValue = it.progressMinValue
            progressMaxValue = it.progressMaxValue
            progressBackgroundColor = it.progressBackgroundColor
            progressColor = it.progressColor
            progressCornerRadius = it.progressCornerRadius
            progressAlpha = it.progressAlpha
        }
    }

    fun setSegmentsQuantity(newCount: Int) {
        segmentCount = newCount
    }

    fun setAutoScrollable(isAutoScrollable: Boolean) {
        this.isAutoScrollable = isAutoScrollable
    }

    private fun initializeProgressBarSegments() {
        segments.clear()
        for (i in 0 until segmentCount) {
            val layoutPms = createSegmentLayoutParamsWithMargins(i)
            segments.add(createProgressSegment(layoutPms))
        }
    }

    private fun createSegmentLayoutParamsWithMargins(i: Int): LayoutParams {
        val layoutPms = LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        if (i < segmentCount - 1) {
            layoutPms.setMargins(0, 0, progressSpacing, 0)
        }
        return layoutPms
    }

    private fun createProgressSegment(layoutPms: LayoutParams) = ProgressSegment(
        ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
            alpha = progressAlpha
            layoutParams = layoutPms
            max = progressMaxValue
            progress = progressMinValue
            progressDrawable = createCustomProgressDrawable()
        }
    )

    private fun createCustomProgressDrawable(): LayerDrawable {
        val backgroundShape = GradientDrawable().apply {
            setColor(progressBackgroundColor)
            cornerRadius = progressCornerRadius.toFloat()
        }

        val progressShape = GradientDrawable().apply {
            setColor(progressColor)
            cornerRadius = progressCornerRadius.toFloat()
        }
        val progressClip = ClipDrawable(progressShape, Gravity.START, ClipDrawable.HORIZONTAL)

        val layers = arrayOf(backgroundShape, progressClip)
        return LayerDrawable(layers)
    }

    private fun drawSegmentedProgressBar() {
        removeAllViews()
        for (i in 0 until segmentCount) {
            val progressBar = segments[i].progressBar.apply {
                if (!isAutoScrollable && i == 0) progress = progressMaxValue
            }
            addView(progressBar)
        }
        invalidate()
    }

    fun launchAnimationSegmentProgress(index: Int, durationProgress: Int) {
        if (index !in segments.indices) return
        cancelAllAnimations()
        val progressBar = segments[index].progressBar
        val animator = ValueAnimator.ofInt(progressMinValue, progressMaxValue).apply {
            this.duration = durationProgress.toLong()
            interpolator = LinearInterpolator()
            addUpdateListener {
                val progress = it.animatedValue as Int
                progressBar.progress = progress
            }
            start()
        }
        segments[index].animator = animator
    }

    fun cancelAllAnimations() {
        segments.forEach { it.animator?.cancel() }
    }

    fun recalculateProgressAfterUserScroll(newPosition: Int) {
        cancelAllAnimations()
        segments.forEachIndexed { index, progressSegment ->
            progressSegment.progressBar.progress =
                if (index < newPosition) progressMaxValue else progressMinValue
        }
    }

    fun fillProgressAfterUserScroll(newPosition: Int) {
        segments.forEachIndexed { index, progressSegment ->
            progressSegment.progressBar.progress =
                if (index <= newPosition) progressMaxValue else progressMinValue
        }
    }

    fun resetAllProgress() {
        cancelAllAnimations()
        segments.forEach { it.progressBar.progress = progressMinValue }
    }

}
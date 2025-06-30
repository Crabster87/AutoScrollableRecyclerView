package io.github.crabster87.autoscrollablerecyclerview

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.crabster87.autoscrollablerecyclerview.ViewHelper.visibility
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AutoScrollableRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private val recyclerView: AdaptedRecyclerView by lazy { findViewById(R.id.auto_scrollable_rv) }
    private val progressBar: SegmentedLinearProgressBar by lazy { findViewById(R.id.segmented_pb) }

    private var jobScroll: Job? = null
    private var displayingDuration: Int = 0
    private var isAutoScrollable: Boolean = true

    private var progressLayoutWidth: Int = 0
    private var progressLayoutHeight: Int = 0
    private var progressLayoutGravity: Int = 0

    private var progressSpacing: Int = 0
    private var progressMinValue: Int = 0
    private var progressMaxValue: Int = 0
    private var progressBackgroundColor: Int = 0
    private var progressColor: Int = 0
    private var progressCornerRadius: Int = 0
    private var progressAlpha: Float = 1f

    private var progressLayoutMarginTop: Int = 0
    private var progressLayoutMarginBottom: Int = 0
    private var progressLayoutMarginStart: Int = 0
    private var progressLayoutMarginEnd: Int = 0

    private val scrollableListSize: Int
        get() = recyclerView.adapter?.itemCount ?: 0

    private val params: LayoutParams
        get() = progressBar.layoutParams as LayoutParams

    init {
        inflate(context, R.layout.autoscrollable_rv_layout, this)
        loadAttributes(attrs)
        applyProgressBarLayoutParams()
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)

        recyclerView.visibility = visibility
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        stopAutoScrolling()
        jobScroll = null
    }

    private fun loadAttributes(attrs: AttributeSet?) {
        context.withStyledAttributes(
            attrs,
            R.styleable.AutoScrollableRecyclerView,
            0,
            0
        ) {
            progressLayoutWidth = getLayoutDimension(
                R.styleable.AutoScrollableRecyclerView_progressLayoutWidth,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            progressLayoutHeight = getLayoutDimension(
                R.styleable.AutoScrollableRecyclerView_progressLayoutHeight,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            progressLayoutGravity = getInteger(
                R.styleable.AutoScrollableRecyclerView_progressLayoutGravity,
                Gravity.NO_GRAVITY
            )
            displayingDuration =
                getInteger(R.styleable.AutoScrollableRecyclerView_displayingDuration, 0)
            isAutoScrollable =
                getBoolean(R.styleable.AutoScrollableRecyclerView_isAutoScrollable, true)
            progressSpacing =
                getDimensionPixelSize(R.styleable.AutoScrollableRecyclerView_progressSpacing, 0)
            progressMinValue =
                getInteger(R.styleable.AutoScrollableRecyclerView_progressMinValue, 0)
            progressMaxValue =
                getInteger(R.styleable.AutoScrollableRecyclerView_progressMaxValue, 100)
            progressBackgroundColor =
                getColor(R.styleable.AutoScrollableRecyclerView_progressBackgroundColor, Color.GRAY)
            progressColor =
                getColor(R.styleable.AutoScrollableRecyclerView_progressColor, Color.BLUE)
            progressCornerRadius =
                getDimensionPixelSize(
                    R.styleable.AutoScrollableRecyclerView_progressCornerRadius,
                    0
                )
            progressAlpha = getFloat(R.styleable.AutoScrollableRecyclerView_progressAlpha, 1f)

            progressLayoutMarginTop = getDimensionPixelSize(
                R.styleable.AutoScrollableRecyclerView_progressLayoutMarginTop,
                0
            )
            progressLayoutMarginBottom = getDimensionPixelSize(
                R.styleable.AutoScrollableRecyclerView_progressLayoutMarginBottom,
                0
            )
            progressLayoutMarginStart = getDimensionPixelSize(
                R.styleable.AutoScrollableRecyclerView_progressLayoutMarginStart,
                0
            )
            progressLayoutMarginEnd = getDimensionPixelSize(
                R.styleable.AutoScrollableRecyclerView_progressLayoutMarginEnd,
                0
            )
        }
    }

    fun <T : Any, VH : RecyclerView.ViewHolder> setAdapter(adapter: ListAdapter<T, VH>?) {
        recyclerView.adapter = adapter
    }

    fun <T : Any, VH : RecyclerView.ViewHolder> submitData(
        adapter: ListAdapter<T, VH>?,
        list: List<T>,
        onCommitted: () -> Unit,
    ) {
        adapter?.let {
            if (isAutoScrollable) {
                it.submitList(list) { onCommitted() }
            } else {
                it.submitList(list)
            }
        }
    }

    fun setOnScrollListener(viewLifecycleOwner: LifecycleOwner) {
        recyclerView.also {
            if (isAutoScrollable) {
                it.setOnScrollListener { withPosition ->
                    launchAutoScrolling(viewLifecycleOwner, withPosition)
                }
            } else {
                it.setOnScrollListener(::observeProgressWithoutAutoScrolling)
            }
        }
    }

    fun launchAutoScrolling(viewLifecycleOwner: LifecycleOwner, withPosition: Int = 0) {
        if (scrollableListSize == 0) return
        if (jobScroll?.isActive == true) jobScroll?.cancel()
        var scrollCounter = withPosition
        progressBar.recalculateProgressAfterUserScroll(scrollCounter)
        jobScroll = viewLifecycleOwner.lifecycleScope.launch {
            while (true) {
                recyclerView.scrollToPosition(scrollCounter)
                progressBar.launchAnimationSegmentProgress(scrollCounter, displayingDuration)
                delay(displayingDuration.toLong())
                scrollCounter++
                if (scrollCounter == 1 && scrollableListSize == 1) return@launch
                if (scrollCounter == scrollableListSize) {
                    scrollCounter = 0
                    progressBar.resetAllProgress()
                }
            }
        }
    }

    fun stopAutoScrolling() {
        progressBar.cancelAllAnimations()
        jobScroll?.cancel()
    }

    fun <T> displaySegmentedLinearProgressBar(
        list: List<T>,
        margins: ProgressBarParams.Margins? = null,
    ) {
        val newCount = list.size
        progressBar.apply {
            layoutParams = updateLayoutParamsWithMargins(margins)
            visibility(newCount > 0)
        }.run {
            setSegmentedLinearProgressBarParams(createProgressBarParams())
            if (newCount > 0) {
                setAutoScrollable(isAutoScrollable)
                setSegmentsQuantity(newCount)
            }
        }
    }

    private fun applyProgressBarLayoutParams() {
        params.apply {
            gravity = progressLayoutGravity
            width = progressLayoutWidth
            height = progressLayoutHeight
        }
        progressBar.layoutParams = params
    }

    private fun createProgressBarParams() = ProgressBarParams(
        progressSpacing = progressSpacing,
        progressMinValue = progressMinValue,
        progressMaxValue = progressMaxValue,
        progressBackgroundColor = progressBackgroundColor,
        progressColor = progressColor,
        progressCornerRadius = progressCornerRadius,
        progressAlpha = progressAlpha,
    )

    private fun updateLayoutParamsWithMargins(margins: ProgressBarParams.Margins?): LayoutParams {
        margins?.let {
            progressLayoutMarginStart = it.progressLayoutMarginStart.convertDpToPx()
            progressLayoutMarginTop = it.progressLayoutMarginTop.convertDpToPx()
            progressLayoutMarginEnd = it.progressLayoutMarginEnd.convertDpToPx()
            progressLayoutMarginBottom = it.progressLayoutMarginBottom.convertDpToPx()
        }
        return params.apply {
            setMargins(
                progressLayoutMarginStart,
                progressLayoutMarginTop,
                progressLayoutMarginEnd,
                progressLayoutMarginBottom
            )
        }
    }

    private fun observeProgressWithoutAutoScrolling(withPosition: Int) {
        if (scrollableListSize == 0) return
        progressBar.fillProgressAfterUserScroll(withPosition)
    }

    private fun Int.convertDpToPx(): Int =
        (this * resources.displayMetrics.density + 0.5f).toInt()

}
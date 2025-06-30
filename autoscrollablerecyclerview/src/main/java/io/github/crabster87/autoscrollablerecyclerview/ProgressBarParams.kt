package io.github.crabster87.autoscrollablerecyclerview

class ProgressBarParams(
    val progressSpacing: Int,
    val progressMinValue: Int,
    val progressMaxValue: Int,
    val progressBackgroundColor: Int,
    val progressColor: Int,
    val progressCornerRadius: Int,
    val progressAlpha: Float,
) {

    class Margins(
        val progressLayoutMarginTop: Int,
        val progressLayoutMarginBottom: Int,
        val progressLayoutMarginStart: Int,
        val progressLayoutMarginEnd: Int,
    )

}
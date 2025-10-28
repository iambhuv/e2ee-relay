package com.promtuz.chat.utils.common

import android.graphics.RectF
import android.os.SystemClock

class OneEuroFilter2D(
    private val minCutoff: Float = 1.0f,
    private val beta: Float = 0.007f,
    private val dCutoff: Float = 1.0f
) {
    private var lastTime = 0L
    private var prevRect: RectF? = null

    // Value filters (for the actual coordinates)
    private var leftFilter = LowPassFilter()
    private var topFilter = LowPassFilter()
    private var rightFilter = LowPassFilter()
    private var bottomFilter = LowPassFilter()

    // Derivative filters
    private var dLeftFilter = LowPassFilter()
    private var dTopFilter = LowPassFilter()
    private var dRightFilter = LowPassFilter()
    private var dBottomFilter = LowPassFilter()

    fun filter(rect: RectF): RectF {
        val now = SystemClock.elapsedRealtime()
        val dt = if (lastTime == 0L) 0f else (now - lastTime) / 1000f
        lastTime = now

        val prev = prevRect
        prevRect = rect

        if (prev == null || dt <= 0f) return rect

        // Calculate derivatives for each dimension
        val dLeft = (rect.left - prev.left) / dt
        val dTop = (rect.top - prev.top) / dt
        val dRight = (rect.right - prev.right) / dt
        val dBottom = (rect.bottom - prev.bottom) / dt

        // Filter derivatives
        val edLeft = dLeftFilter.filter(dLeft, alpha(dt, dCutoff))
        val edTop = dTopFilter.filter(dTop, alpha(dt, dCutoff))
        val edRight = dRightFilter.filter(dRight, alpha(dt, dCutoff))
        val edBottom = dBottomFilter.filter(dBottom, alpha(dt, dCutoff))

        // Adaptive cutoff for each dimension
        val cutoffLeft = minCutoff + beta * kotlin.math.abs(edLeft)
        val cutoffTop = minCutoff + beta * kotlin.math.abs(edTop)
        val cutoffRight = minCutoff + beta * kotlin.math.abs(edRight)
        val cutoffBottom = minCutoff + beta * kotlin.math.abs(edBottom)

        // Filter actual values
        return RectF(
            leftFilter.filter(rect.left, alpha(dt, cutoffLeft)),
            topFilter.filter(rect.top, alpha(dt, cutoffTop)),
            rightFilter.filter(rect.right, alpha(dt, cutoffRight)),
            bottomFilter.filter(rect.bottom, alpha(dt, cutoffBottom))
        )
    }

    private fun alpha(dt: Float, cutoff: Float): Float {
        val tau = 1f / (2f * Math.PI.toFloat() * cutoff)
        return 1f / (1f + tau / dt)
    }

    private class LowPassFilter {
        private var y = 0f
        private var initialized = false
        fun filter(value: Float, alpha: Float): Float {
            y = if (!initialized) { initialized = true; value } else y + alpha * (value - y)
            return y
        }
    }
}
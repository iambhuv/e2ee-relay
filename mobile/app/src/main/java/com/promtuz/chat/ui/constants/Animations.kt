package com.promtuz.chat.ui.constants

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.EaseInOutCirc
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith

object Tweens {
    fun <T> microInteraction(): TweenSpec<T> {
        return tween(150, easing = EaseInOutCirc)
    }
}

object Buttonimations {
    fun labelSlide(): ContentTransform {
        return (slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight }, animationSpec = Tweens.microInteraction()
        ) + fadeIn(Tweens.microInteraction())) togetherWith (slideOutVertically(
            targetOffsetY = { fullHeight -> -fullHeight }, animationSpec = Tweens.microInteraction()
        ) + fadeOut(Tweens.microInteraction()))
    }
}


object Naviganimation {
    private val enterEase = CubicBezierEasing(0.0f, 0.8f, 0.2f, 1.0f)
    private val exitEase = CubicBezierEasing(0.0f, 0.8f, 0.2f, 0.8f)
    private val initialExit = CubicBezierEasing(0.8f, 0.25f, 0.25f, 1.0f)

    fun transitionSpec() = ContentTransform(
        fadeIn(tween(350, 0, enterEase)) + slideInHorizontally(tween(500, 0, enterEase)) { it / 4 },
        fadeOut(tween(300, 0, exitEase))
    )

    fun popTransitionSpec() = ContentTransform(
        fadeIn(tween(300, 0, exitEase)),
        fadeOut(tween(350, 0, initialExit)) + slideOutHorizontally(tween(350, 0, initialExit)) { (it * 0.75f).toInt() })
}

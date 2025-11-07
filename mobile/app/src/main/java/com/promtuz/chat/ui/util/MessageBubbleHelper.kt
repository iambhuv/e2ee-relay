package com.promtuz.chat.ui.util

import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.*
import com.promtuz.chat.ui.components.messageRoundRect

fun DrawScope.rightTailPath(scale: Float) = Pair(
    Offset(size.width, (size.height) - (3f * scale)),
    Path().apply {
        moveTo(2f * scale, 3f * scale)
        lineTo(0f, 3f * scale)
        lineTo(0f, 0f)
        cubicTo(
            0f, 2f * scale,
            2f * scale, 2.7f * scale,
            2.275f * scale, 2.844f * scale
        )
        quadraticTo(
            2.345f * scale, 2.944f * scale,
            2.25f * scale, 3f * scale
        )
        close()
    }
)

fun DrawScope.leftTailPath(scale: Float) = Pair(
    Offset(0f, (size.height) - (3f * scale)),
    Path().apply {
        moveTo(-2f * scale, 3f * scale)
        lineTo(0f, 3f * scale)  // H 0
        lineTo(0f, 0f)  // L 0 0
        cubicTo(
            0f, 2f * scale,
            -2f * scale, 2.7f * scale,
            -2.275f * scale, 2.844f * scale
        )  // C 0 2 -2 2.7 -2.275 2.844
        quadraticTo(
            -2.345f * scale, 2.944f * scale,
            -2.25f * scale, 3f * scale
        )  // Q -2.345 2.944 -2.25 3
        close()  // Z
    }
)

/**
 * Sets background and tail based on fields
 */
fun DrawScope.composeBubble(
    containerColor: Color,
    cornerRadius: Dp,
    isSent: Boolean,
    showTail: Boolean = true,
    tailScale: Float = 10f
) {
    drawPath(Path().apply {
        addRoundRect(messageRoundRect(size, cornerRadius.toPx(), isSent, showTail))
    }, containerColor)

    if (!showTail) return

    val (offset, tailPath) = if(isSent) rightTailPath(tailScale) else leftTailPath(tailScale)

    translate(
        left = offset.x,
        top = offset.y
    ) {
        drawPath(tailPath, color = containerColor)
    }
}

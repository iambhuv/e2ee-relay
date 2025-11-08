package com.promtuz.chat.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.*
import androidx.core.view.doOnLayout
import com.promtuz.chat.ui.views.QrView
import com.promtuz.chat.utils.extensions.then

/**
 * FIXME:
 *  container color looks doesn't look good
 */
@Composable
fun QrCode(
    data: ByteArray,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val containerColor = colors.primaryFixed
    val modulesColor = colors.background

    var isLoading by remember { mutableStateOf(true) }

    val qrAlpha by animateFloatAsState(if (isLoading) 0f else 1f, label = "qr_alpha")

    Box(
        Modifier
            .padding(48.dp)
            .clip(RoundedCornerShape(12))
            .background(containerColor)
    ) {
        AnimatedContent(isLoading, Modifier.align(Alignment.Center)) {
            it.then {
                LoadingIndicator(
                    Modifier
                        .fillMaxWidth(0.5f)
                        .align(Alignment.Center)
                        .aspectRatio(1f),
                    color = modulesColor
                )
            }
        }

        AndroidView(
            { context ->
                QrView(context).apply {
                    setOnQrGeneratedListener {
                        isLoading = false
                    }
                }
            },
            modifier
                .fillMaxWidth()
                .padding(32.dp)
                .aspectRatio(1f)
                .graphicsLayer {
                    alpha = qrAlpha
                },
            { view ->
                view.setContent(data)
                view.doOnLayout {
                    view.setSize(view.width)
                    view.setColor(modulesColor.toArgb())
                }
            })
    }
}
package com.promtuz.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.promtuz.chat.R
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatBottomBar(haze: HazeState, interactionSource: MutableInteractionSource) {
    val colors = MaterialTheme.colorScheme
    val textStyle = MaterialTheme.typography
    val windowInfo = LocalWindowInfo.current

    var message by remember { mutableStateOf("") }

    val insetsPadding = ScaffoldDefaults.contentWindowInsets.asPaddingValues()

    val hazeStyle = HazeStyle(
        colors.surface,
        HazeTint(colors.surface.copy(0.9f)),
        48.dp,
        0f
    )

    Row(
        Modifier
            .fillMaxWidth()
            .hazeEffect(haze, hazeStyle)
            .height(IntrinsicSize.Min)
            .padding(12.dp, 0.dp, 12.dp, 4.dp)
            // TODO: (elegantly) toggle inset bottom padding along with soft keyboard
            .padding(bottom = insetsPadding.calculateBottomPadding())
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surfaceContainerHighest.copy(0.6f))
            .padding(4.dp)
            .heightIn(max = 0.25f * windowInfo.containerDpSize.height)
    ) {
        CompositionLocalProvider(LocalContentColor provides colors.onSurface) {
            BasicTextField(
                value = message,
                onValueChange = { message = it },
                cursorBrush = SolidColor(colors.primary),
                textStyle = textStyle.bodyLargeEmphasized.copy(colors.onSurface, 18.sp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                interactionSource = interactionSource
            ) { innerTextField ->
                Box(Modifier.padding(12.dp, 8.dp), contentAlignment = Alignment.Center) {
                    Box(Modifier.fillMaxWidth()) {
                        if (message.isEmpty()) Text(
                            "Message",
                            style = textStyle.bodyLargeEmphasized.copy(
                                colors.onSurfaceVariant.copy(0.8f),
                                18.sp
                            )
                        )
                        innerTextField()
                    }
                }
            }
        }

        FilledIconButton(
            onClick = {},
            Modifier
                .size(46.dp)
                .align(Alignment.Bottom),
            colors = IconButtonDefaults.filledIconButtonColors(colors.primary),
            shape = RoundedCornerShape(18.dp)
        ) {
            DrawableIcon(R.drawable.i_send, Modifier.size(20.dp))
        }
    }
}
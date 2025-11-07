package com.promtuz.chat.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.unit.*
import com.promtuz.chat.domain.model.UiMessage
import com.promtuz.chat.ui.theme.adjustLight
import com.promtuz.chat.ui.util.composeBubble
import com.promtuz.chat.domain.model.UiMessagePosition as UiMsgPos

@Composable
fun MessageBubble(message: UiMessage) {
    val colors = MaterialTheme.colorScheme
    val textStyle = MaterialTheme.typography

    val containerColor =
        if (message.isSent) adjustLight(colors.primary, -0.065f) else colors.surfaceContainerHigh

    val haveTopMargin = message.position == UiMsgPos.Start || message.position == UiMsgPos.Single
    val showTail = message.position == UiMsgPos.End || message.position == UiMsgPos.Single

    BoxWithConstraints {
        Box(Modifier.height(IntrinsicSize.Min)) {
            Box(Modifier
                .fillMaxSize()
                .clickable {

                }) {
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .padding(top = if (haveTopMargin) 4.dp else 0.dp),
                horizontalArrangement = if (message.isSent) Arrangement.End else Arrangement.Start,
            ) {
                Box(
                    Modifier
                        .widthIn(max = (this@BoxWithConstraints.maxWidth * 0.65f))
                        .drawBehind {
                            val cornerRadius = 14.dp

                            composeBubble(containerColor, cornerRadius, message.isSent, showTail)
                        }
                        .padding(10.dp, 6.dp)
                ) {
                    Text(message.content, style = textStyle.bodyLargeEmphasized)
                }
            }
        }
    }
}
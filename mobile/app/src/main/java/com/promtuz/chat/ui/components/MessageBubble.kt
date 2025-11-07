package com.promtuz.chat.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.unit.dp
import com.promtuz.chat.ui.util.composeBubble

@Composable
fun MessageBubble(text: String) {
    val colors = MaterialTheme.colorScheme
    val textStyle = MaterialTheme.typography

    BoxWithConstraints {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            Box(
                Modifier
                    .widthIn(max = (this@BoxWithConstraints.maxWidth * 0.65f))
                    .drawBehind {
                        val cornerRadius = 14.dp

                        composeBubble(colors.surfaceContainerHigh, cornerRadius, true)
                    }
                    .padding(12.dp, 6.dp)
            ) {
                Text(text, style = textStyle.bodyLargeEmphasized)
            }
        }
    }
}
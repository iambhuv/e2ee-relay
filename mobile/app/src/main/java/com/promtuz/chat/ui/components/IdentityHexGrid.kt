package com.promtuz.chat.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*

@Composable
fun IdentityHexGrid(key: ByteArray) {
    val keyHex = key.toHexString(HexFormat.UpperCase)

    // TODO: add this in "appearance" settings as well
    val contentColor = Color.Black

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
    ) {
        for (r in 0 until 8) {
            Row(Modifier.weight(1f)) {
                for (c in 0 until 8) {
                    val ch = keyHex[r * 8 + c].toString()
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            ch,
                            style = MaterialTheme.typography.titleLargeEmphasized.copy(
                                color = contentColor,
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
package com.promtuz.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.*
import androidx.core.view.doOnLayout
import com.promtuz.chat.domain.model.Identity
import com.promtuz.chat.ui.views.QrView

@Composable
fun IdentityQrCode(
    identity: Identity, modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // TODO: make these customizable as well
    val containerColor = Color.White
    val modulesColor = Color.Black

    val data = remember { identity.toByteArray() }
    val qrView = remember { QrView(context) }

    Box(
        modifier
            .padding(48.dp)
            .clip(RoundedCornerShape(12))
            .background(containerColor)
    ) {
        HorizontalPager(
            rememberPagerState(pageCount = { 2 }),
            key = { it },
            modifier = Modifier
                .padding(32.dp)
                .aspectRatio(1f),
            overscrollEffect = null,
            pageSpacing = 18.dp,
        ) { page ->
            when (page) {
                0 -> AndroidView(
                    factory = { qrView },
                    update = { v ->
                        v.doOnLayout {
                            v.content = data
                            v.sizePx = v.width
                            v.color = modulesColor.toArgb()
                            v.regenerate()
                        }
                    })

                1 -> IdentityHexGrid(identity.key)
            }
        }

    }
}
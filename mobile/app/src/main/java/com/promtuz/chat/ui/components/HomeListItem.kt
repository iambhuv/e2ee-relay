package com.promtuz.chat.ui.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.promtuz.chat.compositions.LocalBackStack
import com.promtuz.chat.navigation.AppRoutes
import com.promtuz.chat.navigation.Navigate
import com.promtuz.chat.ui.theme.adjustLight
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun HomeListItem(
    peer: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val backStack = LocalBackStack.current;

    Box(
        Modifier
            .fillMaxWidth()
            .combinedClickable(
                enabled = true,
                indication = ripple(
                    color = adjustLight(
                        MaterialTheme.colorScheme.background, 0.3f
                    )
                ),
                onClick = {
                    Navigate.chat(backStack, AppRoutes.ChatScreen(peer))
                },
                onLongClick = {

                },
                onClickLabel = null,
                onLongClickLabel = null,
                onDoubleClick = null,
                role = Role.Button,
                interactionSource = interactionSource,
                hapticFeedbackEnabled = true,
            )
            .indication(interactionSource, null)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(peer)

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    peer,
                    fontSize = 16.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.W500,
                    color = MaterialTheme.colorScheme.onBackground.copy(
                        0.95f
                    )
                )
                Text(
                    "Acha",
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(
                        0.75f
                    )
                )
            }
        }
    }
}

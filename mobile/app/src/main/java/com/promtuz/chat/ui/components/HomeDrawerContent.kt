package com.promtuz.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import com.promtuz.chat.R
import com.promtuz.chat.ui.text.avgSizeInStyle
import com.promtuz.chat.utils.extensions.then

@Composable
fun HomeDrawerContent(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val textTheme = MaterialTheme.typography

    BoxWithConstraints {
        val maxWidth = maxWidth * 0.8f

        val list = remember {
            listOf(
                listOf(
                    "My Profile" to R.drawable.i_profile
                ),
                listOf(
                    "Saved Users" to R.drawable.i_contacts,
                    "Blocked Users" to R.drawable.i_user_blocked
                ),
                listOf(
                    "Settings" to R.drawable.oi_settings,
                    "About" to R.drawable.oi_info
                )
            )
        }

        ModalDrawerSheet(
            modifier = Modifier
                .widthIn(min = 200.dp, max = maxWidth)
                .fillMaxWidth()
        ) {
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 12.dp)
            ) {
                itemsIndexed(list) { outerIndex, items ->
                    val (major, minor) = remember { 32 to 15 }

                    (outerIndex != 0).then {
                        Spacer(Modifier.padding(vertical = 8.dp))
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        for (index in 0..(items.size - 1)) {
                            val (label, drawableIcon) = items[index]

                            val clip = when {
                                items.size == 1 -> RoundedCornerShape(major)
                                index == 0 -> RoundedCornerShape(major, major, minor, minor)
                                index == items.lastIndex -> RoundedCornerShape(
                                    minor,
                                    minor,
                                    major,
                                    major
                                )

                                else -> RoundedCornerShape(minor)
                            }

                            val interactionSource = remember { MutableInteractionSource() }

                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(clip)
                                    .background(colors.surfaceContainer)
                                    .combinedClickable(
                                        interactionSource = interactionSource,
                                        indication = ripple(color = colors.surfaceContainerHighest),
                                        onClick = {

                                        },
                                    )
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painterResource(drawableIcon),
                                    label,
                                    Modifier.size(26.dp),
                                    tint = colors.onPrimaryContainer
                                )

                                Text(
                                    label,
                                    style = avgSizeInStyle(textTheme.labelLargeEmphasized, textTheme.bodyLargeEmphasized),
                                    color = colors.onBackground
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
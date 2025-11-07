package com.promtuz.chat.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import com.promtuz.chat.R
import com.promtuz.chat.data.dummy.dummyChats
import com.promtuz.chat.data.remote.QuicClient
import com.promtuz.chat.navigation.Routes
import com.promtuz.chat.presentation.viewmodel.AppVM
import com.promtuz.chat.security.KeyManager
import com.promtuz.chat.ui.activities.Chat
import com.promtuz.chat.ui.activities.ShareIdentity
import com.promtuz.chat.ui.components.Avatar
import com.promtuz.chat.ui.components.DrawableIcon
import com.promtuz.chat.ui.components.TopBar
import com.promtuz.chat.ui.theme.PromtuzTheme
import com.promtuz.chat.ui.util.groupedRoundShape
import com.promtuz.chat.utils.common.parseMessageDate
import org.koin.compose.koinInject
import timber.log.Timber

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun HomeScreen(
    appViewModel: AppVM
) {
    val context = LocalContext.current
    val direction = LocalLayoutDirection.current
    val textTheme = MaterialTheme.typography
    val colors = MaterialTheme.colorScheme
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val navigator = appViewModel.navigator

    LaunchedEffect(drawerState.currentOffset) {
        Timber.tag("HomeDrawer")
            .d("${drawerState.isAnimationRunning} ; ${drawerState.currentOffset}")
    }

    Scaffold(
        topBar = { TopBar(appViewModel) },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallFloatingActionButton({
                    context.startActivity(Intent(context, ShareIdentity::class.java))
                }) {
                    DrawableIcon(
                        R.drawable.i_qr_code_scanner,
                        desc = "QR Code",
                        tint = colors.onPrimaryContainer
                    )
                }
                FloatingActionButton({
                    navigator.push(Routes.SavedUsers)
                }) {
                    DrawableIcon(
                        R.drawable.i_contacts,
                        Modifier.size(32.dp),
                        desc = "Contacts",
                        tint = colors.onPrimaryContainer,
                    )
                }
            }
        }) { innerPadding ->
        LazyColumn(
            Modifier
                .padding(
                    start = innerPadding.calculateLeftPadding(direction),
                    end = innerPadding.calculateRightPadding(direction),
                    top = 0.dp,
                    bottom = 0.dp
                )
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Spacer(Modifier.height(innerPadding.calculateTopPadding()))
            }

            itemsIndexed(dummyChats) { index, chat ->
                val (_, name, msg) = chat

                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(groupedRoundShape(index, dummyChats.size))
                        .background(colors.surfaceContainer.copy(0.75f))
                        .combinedClickable(
                            onClick = {
                                context.startActivity(Intent(context, Chat::class.java).apply {
                                    putExtra("user", chat.identity)
                                })
                            },
                            onLongClick = {

                            }
                        )
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Avatar(name)

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                name,
                                style = textTheme.titleMediumEmphasized,
                                color = colors.onSecondaryContainer
                            )

                            Text(
                                parseMessageDate(msg.timestamp),
                                style = textTheme.bodySmallEmphasized,
                                color = colors.onSecondaryContainer.copy(0.5f)
                            )
                        }

                        msg.content?.let {
                            Text(
                                it,
                                style = textTheme.bodySmallEmphasized,
                                color = colors.onSecondaryContainer.copy(0.7f)
                            )
                        }
                    }
                }
            }


            item {
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

fun formatHex(bytes: ByteArray?, c: Int = 16): String {
    if (bytes == null) return "nil"
    return bytes.asSequence()
        .map { "%02X".format(it) }
        .chunked(c) { it.joinToString(" ") }
        .joinToString("\n")
}

@Composable
fun StatsBox(
    innerPadding: PaddingValues = PaddingValues(0.dp),
    keyManager: KeyManager = koinInject(),
    quicClient: QuicClient = koinInject()
) {
    val status by quicClient.status

    val keys = mapOf(
        "IDENTITY SECRET KEY" to keyManager.getSecretKey(),
        "IDENTITY PUBLIC KEY" to keyManager.getPublicKey(),
        "SERVER PUBLIC KEY" to quicClient.handshake?.serverPublicKey?.bytes
    )

    Column(
        Modifier
            .padding(innerPadding)
            .fillMaxWidth()
            .padding(12.dp)
            .wrapContentHeight()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(12.dp)
    ) {
        Text("State : $status")

        for ((text, bytes) in keys) {
            Text(
                text,
                style = MaterialTheme.typography.bodyMediumEmphasized,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground.copy(0.6f),
                modifier = Modifier.padding(
                    bottom = 4.dp, top = if (text == keys.keys.first()) 0.dp else 8.dp
                )
            )
            Text(
                formatHex(bytes),
                style = MaterialTheme.typography.bodyMediumEmphasized,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
@Preview
private fun HomeScreenPreview(modifier: Modifier = Modifier) {
    PromtuzTheme(darkTheme = true) {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            StatsBox(PaddingValues.Zero)
        }
    }
}
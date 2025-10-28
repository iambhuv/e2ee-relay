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
import androidx.compose.runtime.getValue
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import com.promtuz.chat.R
import com.promtuz.chat.data.remote.QuicClient
import com.promtuz.chat.presentation.viewmodel.AppViewModel
import com.promtuz.chat.security.KeyManager
import com.promtuz.chat.ui.activities.ShareIdentity
import com.promtuz.chat.ui.components.Avatar
import com.promtuz.chat.ui.components.QrCode
import com.promtuz.chat.ui.components.TopBar
import com.promtuz.chat.ui.theme.PromtuzTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun HomeScreen(
    keyManager: KeyManager = koinInject(),
    appViewModel: AppViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val direction = LocalLayoutDirection.current
    val textTheme = MaterialTheme.typography
    val colors = MaterialTheme.colorScheme
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            BoxWithConstraints {
                val maxWidth = maxWidth * 0.8f

                ModalDrawerSheet(
                    modifier = Modifier.widthIn(min = 200.dp, max = maxWidth)
                ) {
                    keyManager.getPublicKey()?.let {
                        Text("Scan Me Please", modifier = Modifier.padding(16.dp))
                        HorizontalDivider()
                        QrCode(it, Modifier.padding(32.dp))
                        HorizontalDivider()
                    }
                }
            }
        },
    ) {
        Scaffold(topBar = { TopBar() }, floatingActionButton = {
            FloatingActionButton({
                context.startActivity(Intent(context, ShareIdentity::class.java))
            }) {
                Icon(
                    painter = painterResource(R.drawable.i_qr_code_scanner),
                    "QR Code",
                    Modifier,
                    MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }) { innerPadding ->

            val users = listOf(
                "John Doe" to "Hello!",
            )



            LazyColumn(
                Modifier
                    .padding(
                        start = innerPadding.calculateLeftPadding(direction),
                        end = innerPadding.calculateRightPadding(direction),
                        top = innerPadding.calculateTopPadding(),
                        bottom = 0.dp
                    )
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    Spacer(Modifier.height(24.dp))
                }

                itemsIndexed(users) { index, (name, msg) ->
                    val (major, minor) = 32 to 15
                    
                    val clip = when {
                        users.size == 1 -> RoundedCornerShape(major)
                        index == 0 -> RoundedCornerShape(major, major, minor, minor)
                        index == users.lastIndex -> RoundedCornerShape(minor, minor, major, major)
                        else -> RoundedCornerShape(minor)
                    }

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(clip)
                            .background(colors.surfaceContainer.copy(0.75f))
                            .combinedClickable(
                                true,
                                onClick = {

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
                            Text(
                                name,
                                style = textTheme.titleMediumEmphasized,
                                color = colors.onSecondaryContainer
                            )

                            Text(
                                msg,
                                style = textTheme.bodySmallEmphasized,
                                color = colors.onSecondaryContainer.copy(0.7f)
                            )
                        }
                    }
                }


                item {
                    Spacer(Modifier.height(24.dp))
                }
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
fun HomeScreenPreview(modifier: Modifier = Modifier) {
    PromtuzTheme(darkTheme = true) {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            StatsBox(PaddingValues.Zero)
        }
    }
}
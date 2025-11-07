package com.promtuz.chat.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import com.promtuz.chat.R
import com.promtuz.chat.data.remote.ConnectionStatus
import com.promtuz.chat.data.remote.QuicClient
import com.promtuz.chat.presentation.viewmodel.AppVM
import com.promtuz.chat.ui.text.calSansfamily
import com.promtuz.chat.ui.theme.gradientScrim
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(appViewModel: AppVM, quicClient: QuicClient = koinInject()) {
    val context = LocalContext.current
    val staticTitle = stringResource(R.string.app_name)
    var dynamicTitle by remember { mutableStateOf(staticTitle) }
    var job by remember { mutableStateOf<Job?>(null) }
    val menuExpanded = remember { mutableStateOf(false) }

    LaunchedEffect(quicClient) {
        snapshotFlow { quicClient.status.value }.collect { newStatus ->
            dynamicTitle = when (newStatus) {
                ConnectionStatus.Disconnected, ConnectionStatus.NetworkError -> staticTitle
                ConnectionStatus.Connecting -> context.getString(R.string.app_status_connecting)
                ConnectionStatus.HandshakeFailed -> context.getString(R.string.app_status_handshake_fail)
                ConnectionStatus.Connected -> {
                    context.getString(R.string.app_status_connected).also {
                        job = launch {
                            delay(1200)
                            if (quicClient.status.value == ConnectionStatus.Connected) {
                                dynamicTitle = staticTitle
                            }
                        }
                    }
                }
            }
        }
    }

    TopAppBar(
        modifier = Modifier
            .background(gradientScrim()),
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        navigationIcon = {
            Image(
                painterResource(R.drawable.logo_colored),
                contentDescription = "Promtuz App Logo",
                modifier = Modifier.padding(horizontal = 12.dp).width(32.dp)
            )
        },
        title = {
            Text(
                dynamicTitle,
                fontFamily = calSansfamily,
                fontSize = 26.sp,
                //textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
            )
        },
        actions = {
            Box {
                IconButton({ menuExpanded.value = !menuExpanded.value }) {
                    DrawableIcon(R.drawable.i_ellipsis_vertical)
                }
                HomeMoreMenu(appViewModel, menuExpanded)
            }
        }
    )
}
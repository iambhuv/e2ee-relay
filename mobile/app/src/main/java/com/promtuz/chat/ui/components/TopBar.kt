package com.promtuz.chat.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.promtuz.chat.R
import com.promtuz.chat.data.remote.ConnectionStatus
import com.promtuz.chat.data.remote.QuicClient
import com.promtuz.chat.ui.text.calSansfamily
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(quicClient: QuicClient = koinInject()) {
    val ctx = LocalContext.current
    val staticTitle = stringResource(R.string.app_name)
    var dynamicTitle by remember { mutableStateOf(staticTitle) }
    var job by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(quicClient) {
        snapshotFlow { quicClient.status.value }.collect { newStatus ->
            dynamicTitle = when (newStatus) {
                ConnectionStatus.Disconnected, ConnectionStatus.NetworkError -> staticTitle
                ConnectionStatus.Connecting -> ctx.getString(R.string.app_status_connecting)
                ConnectionStatus.HandshakeFailed -> ctx.getString(R.string.app_status_handshake_fail)
                ConnectionStatus.Connected -> {
                    ctx.getString(R.string.app_status_connected).also {
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
        modifier = Modifier.padding(horizontal = 16.dp),
        colors = TopAppBarDefaults.topAppBarColors(Color.Transparent),
        navigationIcon = {
            Image(
                painterResource(R.drawable.logo_colored),
                contentDescription = "Promtuz App Logo",
                modifier = Modifier.width(32.dp)
            )
        },
        title = {
            Text(
                dynamicTitle,
                fontFamily = calSansfamily,
                fontSize = 26.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        actions = {
            Avatar("B", 44.dp, RoundedCornerShape(16.dp))
        })
}
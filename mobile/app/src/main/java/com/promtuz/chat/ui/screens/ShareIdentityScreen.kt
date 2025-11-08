package com.promtuz.chat.ui.screens

import android.content.Intent
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.promtuz.chat.R
import com.promtuz.chat.presentation.viewmodel.ShareIdentityVM
import com.promtuz.chat.ui.activities.QrScanner
import com.promtuz.chat.ui.components.IdentityQrCode
import com.promtuz.chat.ui.components.SimpleScreen
import dev.shreyaspatil.capturable.capturable
import dev.shreyaspatil.capturable.controller.rememberCaptureController

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ShareIdentityScreen(
    viewModel: ShareIdentityVM
) {
    val context = LocalContext.current
    val publicIdentity by viewModel.publicIdentity.collectAsState()
    val captureController = rememberCaptureController()
    val colors = MaterialTheme.colorScheme

    SimpleScreen("Share Identity Key") {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(48.dp, Alignment.CenterVertically)
        ) {
            Box(
                Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterHorizontally)
                    .capturable(captureController)
            ) {
                publicIdentity?.let { IdentityQrCode(it) }
            }
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShareQRButton {
                    viewModel.shareQrCode(captureController) { context.startActivity(it) }
                }
                ScanQRButton()
            }
        }
    }
}


@Composable
private fun ColumnScope.ShareQRButton(modifier: Modifier = Modifier, onShare: () -> Unit) {
    Button(
        onShare,
        modifier = modifier
            .fillMaxWidth(0.8f)
            .align(Alignment.CenterHorizontally),
    ) {
        Text(
            "Share QR Code",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLargeEmphasized.copy(fontSize = MaterialTheme.typography.labelLargeEmphasized.fontSize),
        )
    }
}


@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
private fun ColumnScope.ScanQRButton(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    TextButton(
        {
            context.startActivity(Intent(context, QrScanner::class.java))
        },
        modifier = modifier
            .fillMaxWidth(0.8f)
            .align(Alignment.CenterHorizontally),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.i_qr_code_scanner), "QR Code Scanner Icon"
            )

            Text(
                "Scan QR Code",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLargeEmphasized.copy(fontSize = MaterialTheme.typography.labelLargeEmphasized.fontSize),
            )
        }
    }
}
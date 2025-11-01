@file:androidx.annotation.OptIn(ExperimentalGetImage::class) @file:OptIn(
    ExperimentalMaterial3ExpressiveApi::class
)

package com.promtuz.chat.ui.screens

import android.content.Intent
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.promtuz.chat.R
import com.promtuz.chat.data.repository.UserRepository
import com.promtuz.chat.domain.model.Identity
import com.promtuz.chat.presentation.viewmodel.ShareIdentityVM
import com.promtuz.chat.security.KeyManager
import com.promtuz.chat.ui.activities.QrScanner
import com.promtuz.chat.ui.components.BackTopBar
import com.promtuz.chat.ui.components.QrCode
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun ShareIdentityScreen(
    keyManager: KeyManager = koinInject(),
    userRepo: UserRepository = koinInject(),
    viewModel: ShareIdentityVM = koinViewModel()
) {

    val theme = MaterialTheme

    val key = remember { keyManager.getPublicKey() }

//    val data by produceState<User?>(initialValue = null) {
//        value = try {
//            userRepo.getCurrentUser()
//        } catch (_: Exception) {
//            null
//        }
//    }

    val identity = Identity(key.asList())

    Scaffold(
        topBar = { BackTopBar("Share Identity Key") }) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(theme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(48.dp, Alignment.CenterVertically)
        ) {

            QrCode(
                identity.toByteArray(), Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .aspectRatio(1f)
            )

//            Column(
//                Modifier
//                    .fillMaxWidth(0.8f)
//                    .align(Alignment.CenterHorizontally)
//                    .clip(RoundedCornerShape(32.dp))
//                    .background(theme.colorScheme.surfaceContainer)
//                    .padding(32.dp), verticalArrangement = Arrangement.spacedBy(28.dp)
//            ) {
//
////                Box(
////                    Modifier
////                        .fillMaxWidth()
////                        .align(Alignment.CenterHorizontally)
////                        .aspectRatio(1f)
////                ) {
//////                    LoadingIndicator(
//////                        Modifier
//////                            .fillMaxWidth(0.5f)
//////                            .align(Alignment.Center)
//////                            .aspectRatio(1f)
//////                    )
////                    QrCode(
////                        identity.toByteArray(), Modifier
////                            .fillMaxWidth()
////                            .align(Alignment.Center)
////                            .aspectRatio(1f)
////                    )
////                }
////                QrCode(
////                    identity.toByteArray(),
////                    Modifier
////                        .fillMaxWidth()
////                        .align(Alignment.CenterHorizontally)
////                        .aspectRatio(1f)
////                )
//
//                // PublicKeyBytesHex(bytes)
//            }

            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShareQRButton()
                ScanQRButton()
            }
        }
    }
}


@Composable
private fun ColumnScope.ShareQRButton(modifier: Modifier = Modifier) {
    Button(
        {},
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
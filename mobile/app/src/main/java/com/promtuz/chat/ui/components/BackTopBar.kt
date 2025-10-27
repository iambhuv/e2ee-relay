package com.promtuz.chat.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import com.promtuz.chat.R
import com.promtuz.chat.presentation.viewmodel.AppViewModel
import com.promtuz.chat.ui.text.avgSizeInStyle
import org.koin.compose.viewmodel.koinViewModel


/**
 * Only works inside AppActivity
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BackTopBar(
    title: String,
    appViewModel: AppViewModel = koinViewModel(),
) {
    val textTheme = MaterialTheme.typography

    TopAppBar(navigationIcon = {
        IconButton({
            appViewModel.navigator.back()
        }) {
            Icon(
                painter = painterResource(R.drawable.i_back),
                "Go Back",
                Modifier.size(28.dp),
                MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }, title = {
        Text(
            title, style = avgSizeInStyle(
                textTheme.titleLargeEmphasized, textTheme.titleMediumEmphasized
            )
        )
    })
}
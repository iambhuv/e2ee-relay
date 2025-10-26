package com.promtuz.chat.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import com.promtuz.chat.R
import com.promtuz.chat.compositions.LocalNavigator

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BackTopBar(title: @Composable () -> Unit) {
    val navigator = LocalNavigator.current
    TopAppBar(navigationIcon = {
        IconButton({
            navigator.back()
        }) {
            Icon(
                painter = painterResource(R.drawable.i_back),
                "Go Back",
                Modifier.size(28.dp),
                MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }, title = title)
}
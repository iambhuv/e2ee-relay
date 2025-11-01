package com.promtuz.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*

@Composable
fun SimpleScreen(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Scaffold(
        topBar = { BackTopBar(title) }) { innerPadding ->
        Box(
            modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.background),
        ) {
            content()
        }
    }
}
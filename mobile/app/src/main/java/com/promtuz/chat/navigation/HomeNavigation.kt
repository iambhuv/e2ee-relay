package com.promtuz.chat.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.promtuz.chat.ui.components.TopBar
import com.promtuz.chat.ui.screens.HomeScreen


@Composable
fun AppScreen() {
    Scaffold(
        topBar = { TopBar() }
    ) { innerPadding ->
        HomeScreen(innerPadding)
    }
}
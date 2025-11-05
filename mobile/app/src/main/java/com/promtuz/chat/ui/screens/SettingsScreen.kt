package com.promtuz.chat.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.promtuz.chat.presentation.viewmodel.SettingsVM
import com.promtuz.chat.ui.components.FlexibleScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(viewModel: SettingsVM = koinViewModel()) {
    FlexibleScreen({ Text("Settings") }) { padding, scrollBehaviour ->
        Text("Setting Screen")
    }
}
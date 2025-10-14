package com.promtuz.chat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun CallScreen(hazeState: HazeState, innerPadding: PaddingValues, modifier: Modifier = Modifier) {
    Column(
        modifier
            .shadow(20.dp)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .hazeSource(hazeState)
            .padding(innerPadding)
    ) {
        Text("CALLS BYATCH")
    }
}
package com.promtuz.chat.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ChatScreen(userId: String, modifier: Modifier = Modifier) {
    Scaffold { padding ->
        Box(modifier.padding(padding)) {
            Text("Chat with $userId")
        }
    }

}
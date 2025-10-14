package com.promtuz.chat.presentation.state

import androidx.annotation.StringRes
import com.promtuz.chat.R


sealed class WelcomeField {
    object DisplayName : WelcomeField()
    object Status : WelcomeField()
    object Error : WelcomeField()
}

sealed class WelcomeStatus(@param:StringRes val text: Int) {
    object Normal : WelcomeStatus(R.string.status_continue)
    object Trying : WelcomeStatus(R.string.status_trying)
    object Success : WelcomeStatus(R.string.status_success)
}

data class WelcomeUiState(
    val displayName: String,
    val status: WelcomeStatus,
    val errorText: String?
)

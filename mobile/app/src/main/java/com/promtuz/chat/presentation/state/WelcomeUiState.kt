package com.promtuz.chat.presentation.state

import androidx.annotation.StringRes
import com.promtuz.chat.R


sealed class WelcomeField {
    object Nickname : WelcomeField()
    object Status : WelcomeField()
    object Error : WelcomeField()
}

sealed class WelcomeStatus(@param:StringRes val text: Int) {
    object Normal : WelcomeStatus(R.string.welcome_status_normal)
    object Generating : WelcomeStatus(R.string.welcome_status_generating)
    object Connecting : WelcomeStatus(R.string.welcome_status_connecting)
    object Success : WelcomeStatus(R.string.status_success)
}

data class WelcomeUiState(
    val nickname: String,
    val status: WelcomeStatus,
    val errorText: String?
)

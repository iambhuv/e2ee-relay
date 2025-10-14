package com.promtuz.chat.presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.promtuz.chat.presentation.state.WelcomeField
import com.promtuz.chat.presentation.state.WelcomeStatus
import com.promtuz.chat.presentation.state.WelcomeUiState
import com.promtuz.chat.security.KeyManager
import com.promtuz.rust.Core

class WelcomeViewModel(val keyManager: KeyManager) : ViewModel() {
    private val core = Core.getInstance();

    private val _uiState = mutableStateOf(
        WelcomeUiState(
            "",
            WelcomeStatus.Normal,
            null
        )
    )
    val uiState: State<WelcomeUiState> = _uiState

    fun <T> onChange(field: WelcomeField, value: T) {
        _uiState.value = when (field) {
            WelcomeField.DisplayName -> _uiState.value.copy(displayName = value as String)
            WelcomeField.Error -> _uiState.value.copy(errorText = value as String?)
            WelcomeField.Status -> _uiState.value.copy(status = value as WelcomeStatus)
        }
    }


    /**
     * 1. Generating Key Pair
     *    - Requires Native Core Lib
     * 2. Storing Key Pair
     * 3. Registration of Public Key
     * 4. Connecting to Relay Server
     * 5. Finish Welcome.
     **/
    fun `continue`() {
        // Step 1.
        val (secret, public) = core.getStaticKeypair()

        // Step 2.
        keyManager.storeSecretKey(secret) // secret is emptied

        // Step 3.
        // Need a user repository, which will have registering the pub key, modifying the profile etc

        // Step 4.
        // Need to make the server support QUIC first, then make handler in client aswell

        // Step 5.
        // of   course
    }

}
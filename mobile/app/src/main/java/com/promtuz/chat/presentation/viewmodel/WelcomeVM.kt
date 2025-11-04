package com.promtuz.chat.presentation.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.promtuz.chat.data.local.databases.AppDatabase
import com.promtuz.chat.data.local.entities.User
import com.promtuz.chat.data.remote.ConnectionError
import com.promtuz.chat.data.remote.QuicClient
import com.promtuz.chat.presentation.state.WelcomeField
import com.promtuz.chat.presentation.state.WelcomeStatus
import com.promtuz.chat.presentation.state.WelcomeUiState
import com.promtuz.chat.security.KeyManager
import com.promtuz.rust.Core
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WelcomeVM(
    private val keyManager: KeyManager,
    private val core: Core,
    private val application: Application,
    appDatabase: AppDatabase
) : ViewModel(), KoinComponent {
    private val context: Context get() = application.applicationContext
    private val users = appDatabase.userDao()

    lateinit var quicClient: QuicClient

    private val _uiState = mutableStateOf(
        WelcomeUiState(
            "", WelcomeStatus.Normal, null
        )
    )
    val uiState: State<WelcomeUiState> = _uiState

    fun <T> onChange(field: WelcomeField, value: T) {
        _uiState.value = when (field) {
            WelcomeField.Nickname -> _uiState.value.copy(nickname = value as String)
            WelcomeField.Error -> _uiState.value.copy(errorText = value as String?)
            WelcomeField.Status -> _uiState.value.copy(status = value as WelcomeStatus)
        }
    }

    fun `continue`(onSuccess: () -> Unit) {
        onChange(WelcomeField.Status, WelcomeStatus.Generating)

        // Step 1.
        val (secret, public) = core.getStaticKeypair()

        // Step 2.
        keyManager.storeSecretKey(secret) // secret is emptied
        keyManager.storePublicKey(public)

        _uiState.value = _uiState.value.copy(status = WelcomeStatus.Connecting)

        if (!::quicClient.isInitialized) quicClient = inject<QuicClient>().value

        viewModelScope.launch {
            quicClient.connect(context).onSuccess {
                _uiState.value = _uiState.value.copy(status = WelcomeStatus.Success)
                CoroutineScope(Dispatchers.IO).launch {
                    users.insert(User(public, _uiState.value.nickname))
                }
                onSuccess()
            }.onFailure {
                val errorText = when (val e = it) {
                    is ConnectionError.HandshakeFailed -> "Handshake Rejected : ${e.reason}"
                    ConnectionError.NoInternet -> "No Internet"
                    ConnectionError.ServerUnreachable -> "Server Unreachable"
                    ConnectionError.Timeout -> "Connection Timed Out"
                    is ConnectionError.Unknown -> e.exception.toString()
                    else -> it.message
                }

                _uiState.value = _uiState.value.copy(errorText = errorText)
                _uiState.value = _uiState.value.copy(status = WelcomeStatus.Normal)
            }
        }
    }
}
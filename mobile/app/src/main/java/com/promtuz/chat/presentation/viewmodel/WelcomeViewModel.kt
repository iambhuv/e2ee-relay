package com.promtuz.chat.presentation.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
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

class WelcomeViewModel(
    private val keyManager: KeyManager,
    private val core: Core,
    private val application: Application,
    appDatabase: AppDatabase
) : ViewModel(),
    KoinComponent {
    private val context: Context get() = application.applicationContext
    private val users = appDatabase.userDao()

    lateinit var quicClient: QuicClient

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
            WelcomeField.Nickname -> _uiState.value.copy(nickname = value as String)
            WelcomeField.Error -> _uiState.value.copy(errorText = value as String?)
            WelcomeField.Status -> _uiState.value.copy(status = value as WelcomeStatus)
        }
    }

    init {
        core.initLogger()
    }


    /**
     * 1. Generating Key Pair
     *    - Requires Native Core Lib
     * 2. Storing Key Pair
     * 3. Registration of Public Key
     * 4. Connecting to Relay Server
     * 5. Finish Welcome.
     **/
    fun `continue`(onSuccess: () -> Unit) {
        onChange(WelcomeField.Status, WelcomeStatus.Generating)

        // Step 1.
        val (secret, public) = core.getStaticKeypair()

        CoroutineScope(Dispatchers.IO).launch {
            users.insert(User(public.toList(), _uiState.value.nickname))
        }

        // Step 2.
        keyManager.storeSecretKey(secret) // secret is emptied
        keyManager.storePublicKey(public)

        onChange(WelcomeField.Status, WelcomeStatus.Connecting)

        if (!::quicClient.isInitialized) quicClient = inject<QuicClient>().value

        // TODO: Setup Connection State Listener, to update the UI ig?
        quicClient.connect(context, object : QuicClient.Listener {
            override fun onConnectionFailure(e: ConnectionError) {
                // reverting
                onChange(WelcomeField.Status, WelcomeStatus.Normal)

                when (e) {
                    is ConnectionError.HandshakeFailed -> {
                        onChange(WelcomeField.Error, "Handshake Rejected : ${e.reason}")
                    }

                    ConnectionError.NoInternet -> {
                        onChange(WelcomeField.Error, "No Internet")
                    }

                    ConnectionError.ServerUnreachable -> {
                        onChange(WelcomeField.Error, "Server Unreachable")
                    }

                    ConnectionError.Timeout -> {
                        onChange(WelcomeField.Error, "Connection Timed Out")
                    }

                    is ConnectionError.Unknown -> {
                        onChange(WelcomeField.Error, e.exception.toString())
                    }
                }
            }

            override fun onConnectionSuccess() {
                onChange(WelcomeField.Status, WelcomeStatus.Success)

                onSuccess()
            }
        })
    }

}
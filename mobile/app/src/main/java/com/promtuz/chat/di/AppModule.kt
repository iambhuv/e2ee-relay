package com.promtuz.chat.di

import com.promtuz.chat.data.remote.QuicClient
import com.promtuz.chat.data.remote.realtime.Handshake
import com.promtuz.chat.presentation.viewmodel.WelcomeViewModel
import com.promtuz.chat.security.KeyManager
import com.promtuz.rust.Core
import com.promtuz.rust.Crypto
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

import org.koin.core.module.dsl.viewModel
import tech.kwik.core.QuicClientConnection

val appModule = module {
    single<KeyManager> { KeyManager(androidContext()) }
    single { Core() }
    single { Crypto() }

    factory { (conn: QuicClientConnection) -> Handshake(get(), get(), conn) }

    factory { QuicClient(get(), get()) }

    viewModel { WelcomeViewModel(get(), get()) }
}

val authModule = module {

}
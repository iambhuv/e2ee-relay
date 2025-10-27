package com.promtuz.chat.di

import com.promtuz.chat.data.remote.QuicClient
import com.promtuz.chat.navigation.AppNavigator
import com.promtuz.chat.presentation.viewmodel.AppViewModel
import com.promtuz.chat.presentation.viewmodel.WelcomeViewModel
import com.promtuz.chat.security.KeyManager
import com.promtuz.rust.Core
import com.promtuz.rust.Crypto
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

import org.koin.core.module.dsl.viewModel

val appModule = module {
    single<KeyManager> { KeyManager(androidContext()) }
    single { Core() }
    single { Crypto(get()) }

    single { QuicClient(get(), get()) }

    // single { AppNavigator(get()) }

    viewModel { WelcomeViewModel(get(), get(), get()) }
    viewModel { AppViewModel(get()) }
}

val authModule = module {

}
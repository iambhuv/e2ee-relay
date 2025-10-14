package com.promtuz.chat.di

import com.promtuz.chat.presentation.viewmodel.WelcomeViewModel
import com.promtuz.chat.security.KeyManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

import org.koin.core.module.dsl.viewModel


val appModule = module {
    single<KeyManager> { KeyManager(androidContext()) }

    viewModel { WelcomeViewModel(get()) }
}

val authModule = module {

}
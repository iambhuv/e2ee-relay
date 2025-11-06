package com.promtuz.chat.di

import com.promtuz.chat.presentation.viewmodel.*
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val vmModule = module {
    viewModel { WelcomeVM(get(), get(), get(), get()) }
    viewModel { AppVM(get()) }
    viewModel { ShareIdentityVM(get(), get(), get()) }
    viewModel { QrScannerVM(get(), get()) }
    viewModel { SavedUsersVM(get(), get()) }
    viewModel { SettingsVM(get()) }
    viewModel { ChatVM(get()) }
}
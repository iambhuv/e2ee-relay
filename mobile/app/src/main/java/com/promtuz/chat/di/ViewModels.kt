package com.promtuz.chat.di

import com.promtuz.chat.presentation.viewmodel.AppVM
import com.promtuz.chat.presentation.viewmodel.QrScannerVM
import com.promtuz.chat.presentation.viewmodel.ShareIdentityVM
import com.promtuz.chat.presentation.viewmodel.WelcomeVM
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val vmModule = module {
    viewModel { WelcomeVM(get(), get(), get(), get()) }
    viewModel { AppVM(get()) }
    viewModel { ShareIdentityVM(get(), get(), get()) }
    viewModel { QrScannerVM(get()) }
}
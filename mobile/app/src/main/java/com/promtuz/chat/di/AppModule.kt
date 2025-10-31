package com.promtuz.chat.di

import androidx.room.Room
import com.promtuz.chat.data.local.databases.APP_DB_NAME
import com.promtuz.chat.data.local.databases.AppDatabase
import com.promtuz.chat.data.remote.QuicClient
import com.promtuz.chat.presentation.viewmodel.AppViewModel
import com.promtuz.chat.presentation.viewmodel.WelcomeViewModel
import com.promtuz.chat.security.KeyManager
import com.promtuz.rust.Core
import com.promtuz.rust.Crypto
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<KeyManager> { KeyManager(androidContext()) }
    single { Core() }
    single { Crypto(get()) }

    single { QuicClient(get(), get()) }

    single {
        Room.databaseBuilder(
            get(), AppDatabase::class.java, APP_DB_NAME
        ).build()
    }

    viewModel { WelcomeViewModel(get(), get(), get(), get()) }
    viewModel { AppViewModel(get()) }
}

val authModule = module {

}
package com.promtuz.chat.di

import com.promtuz.chat.data.remote.QuicClient
import com.promtuz.chat.security.KeyManager
import com.promtuz.chat.utils.media.ImageUtils
import com.promtuz.rust.Core
import com.promtuz.rust.Crypto
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { KeyManager(androidContext()) }
    single { Core() }
    single { Crypto() }

    single { QuicClient(get(), get()) }

    single { ImageUtils(get()) }
}
package com.promtuz.chat.di

import com.promtuz.chat.data.remote.NetworkClient
import org.koin.dsl.module

val dataModule = module {
    single<NetworkClient> { NetworkClient() }
}
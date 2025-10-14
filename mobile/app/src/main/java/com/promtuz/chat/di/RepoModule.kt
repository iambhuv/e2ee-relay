package com.promtuz.chat.di

import com.promtuz.chat.data.repository.AuthRepositoryImpl
import org.koin.dsl.module

val repoModule = module {
    single<AuthRepositoryImpl> { AuthRepositoryImpl(get()) }
}
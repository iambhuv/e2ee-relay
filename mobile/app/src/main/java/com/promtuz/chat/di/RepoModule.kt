package com.promtuz.chat.di

import com.promtuz.chat.data.local.dao.UserDao
import com.promtuz.chat.data.repository.UserRepository
import com.promtuz.chat.security.KeyManager
import org.koin.dsl.module

val repoModule = module {
    single { UserRepository(get<UserDao>(), get<KeyManager>()) }
}
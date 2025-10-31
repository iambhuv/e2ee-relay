package com.promtuz.chat.di

import androidx.room.Room
import com.promtuz.chat.data.local.databases.APP_DB_NAME
import com.promtuz.chat.data.local.databases.AppDatabase
import org.koin.dsl.module

val dbModule = module {
    single {
        Room.databaseBuilder(
            get(), AppDatabase::class.java, APP_DB_NAME
        ).build()
    }
    single {
        get<AppDatabase>().userDao()
    }
}
package com.promtuz.chat

import android.app.Application
import com.promtuz.chat.di.appModule
import com.promtuz.chat.di.authModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class Promtuz : Application() {
    override fun onCreate() {
        startKoin {
            androidLogger()
            androidContext(this@Promtuz)
            modules(
                appModule,
                authModule
            )
        }

        super.onCreate()
    }
}
package com.promtuz.chat

import android.app.Application
import android.content.pm.ApplicationInfo
import com.promtuz.chat.di.appModule
import com.promtuz.chat.di.dbModule
import com.promtuz.chat.di.repoModule
import com.promtuz.chat.di.vmModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class Promtuz : Application() {
    override fun onCreate() {

        Timber.plant(Timber.DebugTree())

        startKoin {
            androidLogger()
            androidContext(this@Promtuz)
            modules(
                appModule,
                dbModule,
                vmModule,
                repoModule
            )
        }

        super.onCreate()
    }

    private fun isDebuggable(): Boolean {
        return 0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
    }
}
package com.typ.hearforme

import android.app.Application
import com.typ.hearforme.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class HearForMeApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@HearForMeApp)
            modules(appModule)
        }
    }
}

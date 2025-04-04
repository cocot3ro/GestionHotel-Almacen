package com.cocot3ro.gh.almacen

import android.app.Application
import com.cocot3ro.gh.almacen.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class GhAlmacen : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidLogger()
            androidContext(this@GhAlmacen)
        }
    }
}
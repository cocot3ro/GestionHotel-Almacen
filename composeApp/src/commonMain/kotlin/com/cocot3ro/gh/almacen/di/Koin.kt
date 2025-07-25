package com.cocot3ro.gh.almacen.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.ksp.generated.module

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            nativeModule,
            ViewModelModule.module,
            UseCaseModule.module,
            DatastoreModule.module,
            SessionModule.module,
            networkModule
        )
    }
}
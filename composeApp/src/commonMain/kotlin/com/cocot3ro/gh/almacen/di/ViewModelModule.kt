package com.cocot3ro.gh.almacen.di

import com.cocot3ro.gh.almacen.ui.screens.setup.SetupViewModel
import com.cocot3ro.gh.almacen.ui.screens.splash.SplashViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::SplashViewModel)
    viewModelOf(::SetupViewModel)
}
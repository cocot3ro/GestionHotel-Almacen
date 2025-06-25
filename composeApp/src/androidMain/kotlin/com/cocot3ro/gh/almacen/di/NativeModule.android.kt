package com.cocot3ro.gh.almacen.di

import com.cocot3ro.gh.almacen.ui.screens.setup.SetupCameraViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

actual val nativeModule: Module = module {
    viewModelOf(::SetupCameraViewModel)
}
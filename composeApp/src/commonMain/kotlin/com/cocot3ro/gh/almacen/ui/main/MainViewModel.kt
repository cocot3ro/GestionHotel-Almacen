package com.cocot3ro.gh.almacen.ui.main

import androidx.lifecycle.ViewModel
import com.cocot3ro.gh.almacen.domain.usecase.ManageLoginUsecase
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class MainViewModel(
    @Provided private val manageLoginUsecase: ManageLoginUsecase
) : ViewModel() {

    suspend fun logOut() {
        manageLoginUsecase.logOut()
    }
}
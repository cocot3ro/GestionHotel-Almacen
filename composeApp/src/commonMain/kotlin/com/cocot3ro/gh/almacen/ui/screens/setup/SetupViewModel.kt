package com.cocot3ro.gh.almacen.ui.screens.setup

import androidx.lifecycle.ViewModel
import com.cocot3ro.gh.almacen.domain.usecase.CompleteSetupUseCase
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class SetupViewModel(
    @Provided private val completeSetupUseCase: CompleteSetupUseCase,
) : ViewModel() {

    suspend fun completeSetUp() {
        completeSetupUseCase()
    }

}
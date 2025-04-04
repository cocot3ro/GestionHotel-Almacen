package com.cocot3ro.gh.almacen.ui.screens.setup

import androidx.lifecycle.ViewModel
import com.cocot3ro.gh.almacen.domain.usecase.CompleteSetupUseCase

class SetupViewModel(
    private val completeSetupUseCase: CompleteSetupUseCase,
) : ViewModel() {

    suspend fun completeSetUp() {
        completeSetupUseCase()
    }

}
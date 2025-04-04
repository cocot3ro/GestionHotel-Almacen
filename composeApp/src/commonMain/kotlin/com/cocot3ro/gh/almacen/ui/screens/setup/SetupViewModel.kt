package com.cocot3ro.gh.almacen.ui.screens.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocot3ro.gh.almacen.domain.usecase.CompleteSetupUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SetupViewModel(
    private val completeSetupUseCase: CompleteSetupUseCase,
) : ViewModel() {

    fun completeSetUp() {
        viewModelScope.launch(Dispatchers.IO) {
            completeSetupUseCase()
        }
    }

}
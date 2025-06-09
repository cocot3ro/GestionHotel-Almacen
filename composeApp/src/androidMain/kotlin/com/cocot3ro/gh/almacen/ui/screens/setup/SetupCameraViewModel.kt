package com.cocot3ro.gh.almacen.ui.screens.setup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SetupCameraViewModel : ViewModel() {

    var canContinue: Boolean by mutableStateOf(false)
        private set

    var iWasRationale: Boolean by mutableStateOf(false)
        private set

    var permanentDenied: Boolean by mutableStateOf(false)
        private set

    fun updateCanContinue(canContinue: Boolean) {
        this.canContinue = canContinue
    }

    fun updateIWasRationale(iWasRationale: Boolean) {
        this.iWasRationale = iWasRationale
    }

    fun updatePermanentDenied(permanentDenied: Boolean) {
        this.permanentDenied = permanentDenied
    }
}
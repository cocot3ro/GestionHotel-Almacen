package com.cocot3ro.gh.almacen.domain.state

import com.cocot3ro.gh.almacen.domain.model.VersionInfoDomain

sealed class SplashUiState {

    data object Idle : SplashUiState()
    data object Loading : SplashUiState()
    data object SetupRequired : SplashUiState()
    data class UpdateRequired(val versionInfo: VersionInfoDomain) : SplashUiState()
    data object SplashUiFinished : SplashUiState()
    data class Error(val message: String, val cause: Throwable) : SplashUiState()

}
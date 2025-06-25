package com.cocot3ro.gh.almacen.ui.screens.login

import com.cocot3ro.gh.almacen.domain.model.AlmacenUserDomain

sealed class LoginUiState {

    data object Idle : LoginUiState()
    data class Waiting(val user: AlmacenUserDomain) : LoginUiState()
    data class Loading(val user: AlmacenUserDomain) : LoginUiState()
    data class Success(val user: AlmacenUserDomain) : LoginUiState()
    data class Fail(val user: AlmacenUserDomain) : LoginUiState()
    data class Error(val user: AlmacenUserDomain, val cause: Throwable) : LoginUiState()
}
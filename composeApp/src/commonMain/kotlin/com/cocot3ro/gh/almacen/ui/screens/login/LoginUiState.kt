package com.cocot3ro.gh.almacen.ui.screens.login

import com.cocot3ro.gh.almacen.domain.model.UserDomain

sealed class LoginUiState {

    data object Idle : LoginUiState()
    data class Waiting(val user: UserDomain) : LoginUiState()
    data class Loading(val user: UserDomain) : LoginUiState()
    data class Success(val user: UserDomain) : LoginUiState()
    data class Fail(val user: UserDomain) : LoginUiState()
    data class Error(val user: UserDomain, val cause: Throwable) : LoginUiState()
}
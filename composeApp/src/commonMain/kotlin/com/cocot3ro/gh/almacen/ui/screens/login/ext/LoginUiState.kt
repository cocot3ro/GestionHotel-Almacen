package com.cocot3ro.gh.almacen.ui.screens.login.ext

import com.cocot3ro.gh.almacen.domain.model.UserDomain
import com.cocot3ro.gh.almacen.ui.screens.login.LoginUiState

fun LoginUiState.getUser(): UserDomain? = when (this) {
    is LoginUiState.Idle -> null
    is LoginUiState.Waiting -> user
    is LoginUiState.Loading -> user
    is LoginUiState.Success -> user
    is LoginUiState.Fail -> user
    is LoginUiState.Error -> user
}
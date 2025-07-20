package com.cocot3ro.gh.almacen.domain.state.ext

import com.cocot3ro.gh.almacen.domain.model.AlmacenStoreDomain
import com.cocot3ro.gh.almacen.domain.model.UserDomain
import com.cocot3ro.gh.almacen.domain.state.LoginUiState

fun LoginUiState.getUser(): UserDomain? = when (this) {
    is LoginUiState.Idle -> null
    is LoginUiState.Waiting -> user
    is LoginUiState.Loading -> user
    is LoginUiState.Success -> user
    is LoginUiState.Fail -> user
    is LoginUiState.Error -> user
}

fun LoginUiState.getStore(): AlmacenStoreDomain? = when (this) {
    is LoginUiState.Idle -> null
    is LoginUiState.Waiting -> store
    is LoginUiState.Loading -> store
    is LoginUiState.Success -> store
    is LoginUiState.Fail -> store
    is LoginUiState.Error -> store
}

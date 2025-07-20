package com.cocot3ro.gh.almacen.domain.state

import com.cocot3ro.gh.almacen.domain.model.AlmacenStoreDomain
import com.cocot3ro.gh.almacen.domain.model.UserDomain

sealed class LoginUiState {

    data object Idle : LoginUiState()

    data class Waiting(
        val user: UserDomain?,
        val store: AlmacenStoreDomain?
    ) : LoginUiState()

    data class Loading(
        val user: UserDomain,
        val store: AlmacenStoreDomain
    ) : LoginUiState()

    data class Success(
        val user: UserDomain,
        val store: AlmacenStoreDomain
    ) : LoginUiState()

    data class Fail(
        val user: UserDomain,
        val store: AlmacenStoreDomain
    ) : LoginUiState()

    data class Error(
        val user: UserDomain,
        val store: AlmacenStoreDomain,
        val cause: Throwable
    ) : LoginUiState()
}
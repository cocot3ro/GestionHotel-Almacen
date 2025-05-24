package com.cocot3ro.gh.almacen.domain.state

import com.cocot3ro.gh.almacen.domain.model.AlmacenLoginResponseDomain

sealed class LoginResult {

    data class Success(val loginResponse: AlmacenLoginResponseDomain) : LoginResult()
    data object Unauthorized : LoginResult()
    data class Error(val cause: Throwable) : LoginResult()

}
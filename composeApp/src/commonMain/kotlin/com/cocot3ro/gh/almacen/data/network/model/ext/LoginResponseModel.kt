package com.cocot3ro.gh.almacen.data.network.model.ext

import com.cocot3ro.gh.almacen.domain.model.LoginResponseDomain
import com.cocot3ro.gh.services.login.LoginResponseModel

fun LoginResponseModel.toDomain(): LoginResponseDomain = LoginResponseDomain(
    userId = userId,
    jwtToken = jwtToken,
    refreshToken = refreshToken,
    expiresAt = expiresAt
)
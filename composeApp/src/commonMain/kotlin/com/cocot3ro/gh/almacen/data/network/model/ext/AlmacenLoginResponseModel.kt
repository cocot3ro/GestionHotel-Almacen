package com.cocot3ro.gh.almacen.data.network.model.ext

import com.cocot3ro.gh.almacen.data.network.model.AlmacenLoginResponseModel
import com.cocot3ro.gh.almacen.domain.model.AlmacenLoginResponseDomain

fun AlmacenLoginResponseModel.toDomain(): AlmacenLoginResponseDomain = AlmacenLoginResponseDomain(
    userId = userId,
    jwtToken = jwtToken,
    refreshToken = refreshToken,
    expiresAt = expiresAt
)
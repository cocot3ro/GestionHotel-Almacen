package com.cocot3ro.gh.almacen.domain.model

data class AlmacenLoginResponseDomain(
    val userId: Long,
    val jwtToken: String,
    val refreshToken: String,
    val expiresAt: String
)

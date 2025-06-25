package com.cocot3ro.gh.almacen.domain.model

data class AuthPreferenceItem(
    val jwtToken: String?,
    val refreshToken: String?
)

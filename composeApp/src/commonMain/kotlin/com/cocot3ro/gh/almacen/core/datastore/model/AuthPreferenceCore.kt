package com.cocot3ro.gh.almacen.core.datastore.model

data class AuthPreferenceCore(
    val jwtToken: String?,
    val refreshToken: String?
)

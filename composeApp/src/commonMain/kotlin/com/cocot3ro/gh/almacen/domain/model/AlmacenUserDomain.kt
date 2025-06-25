package com.cocot3ro.gh.almacen.domain.model

data class AlmacenUserDomain(
    val id: Long = 0L,
    val name: String,
    val image: String?,
    val requiresPassword: Boolean,
    val role: UserRoleDomain
)
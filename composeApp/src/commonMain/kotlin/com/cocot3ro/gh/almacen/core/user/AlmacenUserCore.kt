package com.cocot3ro.gh.almacen.core.user

data class AlmacenUserCore(
    val userId: Long,
    val name: String,
    val image: String?,
    val requiresPassword: Boolean,
    val role: UserRoleCore
)
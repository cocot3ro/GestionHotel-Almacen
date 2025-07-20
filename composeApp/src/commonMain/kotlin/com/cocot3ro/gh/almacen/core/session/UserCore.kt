package com.cocot3ro.gh.almacen.core.session

data class UserCore(
    val userId: Long,
    val name: String,
    val image: String?,
    val requiresPassword: Boolean,
    val role: UserRoleCore
)
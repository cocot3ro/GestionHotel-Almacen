package com.cocot3ro.gh.almacen.core.user.ext

import com.cocot3ro.gh.almacen.core.user.UserRoleCore
import com.cocot3ro.gh.almacen.domain.model.UserRoleDomain

fun UserRoleCore.toDomain(): UserRoleDomain = when (this) {
    UserRoleCore.ADMIN -> UserRoleDomain.ADMIN
    UserRoleCore.USER -> UserRoleDomain.USER
}
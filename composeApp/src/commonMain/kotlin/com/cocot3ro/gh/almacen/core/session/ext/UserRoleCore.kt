package com.cocot3ro.gh.almacen.core.session.ext

import com.cocot3ro.gh.almacen.core.session.UserRoleCore
import com.cocot3ro.gh.almacen.domain.model.UserRoleDomain

fun UserRoleCore.toDomain(): UserRoleDomain = when (this) {
    UserRoleCore.ADMIN -> UserRoleDomain.ADMIN
    UserRoleCore.USER -> UserRoleDomain.USER
}
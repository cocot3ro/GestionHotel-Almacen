package com.cocot3ro.gh.almacen.domain.model.ext

import com.cocot3ro.gh.almacen.core.user.UserRoleCore
import com.cocot3ro.gh.almacen.domain.model.UserRoleDomain
import com.cocot3ro.gh.services.users.UserRoleModel

fun UserRoleDomain.toModel(): UserRoleModel = when (this) {
    UserRoleDomain.ADMIN -> UserRoleModel.ADMIN
    UserRoleDomain.USER -> UserRoleModel.USER
}

fun UserRoleDomain.toCore(): UserRoleCore = when (this) {
    UserRoleDomain.ADMIN -> UserRoleCore.ADMIN
    UserRoleDomain.USER -> UserRoleCore.USER
}
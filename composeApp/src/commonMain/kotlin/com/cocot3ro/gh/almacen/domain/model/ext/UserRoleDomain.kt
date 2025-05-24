package com.cocot3ro.gh.almacen.domain.model.ext

import com.cocot3ro.gh.almacen.core.user.UserRoleCore
import com.cocot3ro.gh.almacen.data.network.model.UserRoleModel
import com.cocot3ro.gh.almacen.domain.model.UserRoleDomain

fun UserRoleDomain.toModel(): UserRoleModel = when (this) {
    UserRoleDomain.ADMIN -> UserRoleModel.ADMIN
    UserRoleDomain.USER -> UserRoleModel.USER
}

fun UserRoleDomain.toCore(): UserRoleCore = when (this) {
    UserRoleDomain.ADMIN -> UserRoleCore.ADMIN
    UserRoleDomain.USER -> UserRoleCore.USER
}
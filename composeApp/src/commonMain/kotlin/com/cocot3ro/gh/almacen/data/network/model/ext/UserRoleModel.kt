package com.cocot3ro.gh.almacen.data.network.model.ext

import com.cocot3ro.gh.almacen.data.network.model.UserRoleModel
import com.cocot3ro.gh.almacen.domain.model.UserRoleDomain

fun UserRoleModel.toDomain(): UserRoleDomain = when (this) {
    UserRoleModel.ADMIN -> UserRoleDomain.ADMIN
    UserRoleModel.USER -> UserRoleDomain.USER
}
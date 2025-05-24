package com.cocot3ro.gh.almacen.data.database.ext

import com.cocot3ro.gh.almacen.data.database.model.UserRoleEntity
import com.cocot3ro.gh.almacen.data.network.model.UserRoleModel

fun UserRoleEntity.toModel(): UserRoleModel = when (this) {
    UserRoleEntity.ADMIN -> UserRoleModel.ADMIN
    UserRoleEntity.USER -> UserRoleModel.USER
}
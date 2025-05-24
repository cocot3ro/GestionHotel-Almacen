package com.cocot3ro.gh.almacen.data.network.model.ext

import com.cocot3ro.gh.almacen.data.database.model.UserRoleEntity
import com.cocot3ro.gh.almacen.data.network.model.UserRoleModel

fun UserRoleModel.toDatabase(): UserRoleEntity = when (this) {
    UserRoleModel.ADMIN -> UserRoleEntity.ADMIN
    UserRoleModel.USER -> UserRoleEntity.USER
}
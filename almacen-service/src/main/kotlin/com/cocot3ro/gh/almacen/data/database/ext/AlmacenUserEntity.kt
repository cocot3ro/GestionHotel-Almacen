package com.cocot3ro.gh.almacen.data.database.ext

import com.cocot3ro.gh.almacen.AlmacenUserEntity
import com.cocot3ro.gh.almacen.data.network.model.AlmacenUserModel

fun AlmacenUserEntity.toModel() = AlmacenUserModel(
    id = id,
    name = name,
    image = image,
    requiresPassword = this.passwordHash != null,
    role = role.toModel()
)
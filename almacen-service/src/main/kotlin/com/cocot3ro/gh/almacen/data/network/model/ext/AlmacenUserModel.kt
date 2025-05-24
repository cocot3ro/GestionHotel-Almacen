package com.cocot3ro.gh.almacen.data.network.model.ext

import com.cocot3ro.gh.almacen.AlmacenUserEntity
import com.cocot3ro.gh.almacen.data.network.model.AlmacenUserModel

fun AlmacenUserModel.toDatabase(): AlmacenUserEntity = AlmacenUserEntity(
    id = id,
    name = name,
    image = image,
    passwordHash = null,
    role = role.toDatabase()
)

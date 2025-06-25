package com.cocot3ro.gh.almacen.data.network.model.ext

import com.cocot3ro.gh.almacen.data.network.model.AlmacenUserModel
import com.cocot3ro.gh.almacen.domain.model.AlmacenUserDomain

fun AlmacenUserModel.toDomain() = AlmacenUserDomain(
    id = id,
    name = name,
    image = image,
    requiresPassword = requiresPassword,
    role = role.toDomain()
)

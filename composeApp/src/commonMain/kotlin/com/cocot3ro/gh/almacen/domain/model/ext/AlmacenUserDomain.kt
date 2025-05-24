package com.cocot3ro.gh.almacen.domain.model.ext

import com.cocot3ro.gh.almacen.core.user.AlmacenUserCore
import com.cocot3ro.gh.almacen.data.network.model.AlmacenUserModel
import com.cocot3ro.gh.almacen.domain.model.AlmacenUserDomain

fun AlmacenUserDomain.toCore() = AlmacenUserCore(
    userId = id,
    name = name,
    image = image,
    requiresPassword = requiresPassword,
    role = role.toCore()
)

fun AlmacenUserDomain.toModel() = AlmacenUserModel(
    id = id,
    name = name,
    image = image,
    requiresPassword = requiresPassword,
    role = role.toModel()
)
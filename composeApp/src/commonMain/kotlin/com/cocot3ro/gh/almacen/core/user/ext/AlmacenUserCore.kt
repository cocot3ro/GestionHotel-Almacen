package com.cocot3ro.gh.almacen.core.user.ext

import com.cocot3ro.gh.almacen.core.user.AlmacenUserCore
import com.cocot3ro.gh.almacen.domain.model.AlmacenUserDomain

fun AlmacenUserCore.toDomain(): AlmacenUserDomain = AlmacenUserDomain(
    id = userId,
    name = name,
    image = image,
    requiresPassword = requiresPassword,
    role = role.toDomain()
)
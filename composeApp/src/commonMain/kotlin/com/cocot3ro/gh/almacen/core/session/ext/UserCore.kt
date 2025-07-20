package com.cocot3ro.gh.almacen.core.session.ext

import com.cocot3ro.gh.almacen.core.session.UserCore
import com.cocot3ro.gh.almacen.domain.model.UserDomain

fun UserCore.toDomain(): UserDomain = UserDomain(
    id = userId,
    name = name,
    image = image,
    requiresPassword = requiresPassword,
    role = role.toDomain()
)
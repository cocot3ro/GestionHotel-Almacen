package com.cocot3ro.gh.almacen.domain.model.ext

import com.cocot3ro.gh.almacen.core.user.AlmacenUserCore
import com.cocot3ro.gh.almacen.domain.model.AlmacenUserDomain
import com.cocot3ro.gh.services.users.UserModel

fun AlmacenUserDomain.toCore() = AlmacenUserCore(
    userId = id,
    name = name,
    image = image,
    requiresPassword = requiresPassword,
    role = role.toCore()
)

fun AlmacenUserDomain.toModel() = UserModel(
    id = id,
    name = name,
    image = image?.replace("""^https?://[^/]+""".toRegex(), ""),
    requiresPassword = requiresPassword,
    role = role.toModel()
)
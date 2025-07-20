package com.cocot3ro.gh.almacen.domain.model.ext

import com.cocot3ro.gh.almacen.core.session.UserCore
import com.cocot3ro.gh.almacen.domain.model.UserDomain
import com.cocot3ro.gh.services.users.UserModel

fun UserDomain.toCore() = UserCore(
    userId = id,
    name = name,
    image = image,
    requiresPassword = requiresPassword,
    role = role.toCore()
)

fun UserDomain.toModel() = UserModel(
    id = id,
    name = name,
    image = image?.replace("""^https?://[^/]+""".toRegex(), ""),
    requiresPassword = requiresPassword,
    role = role.toModel()
)
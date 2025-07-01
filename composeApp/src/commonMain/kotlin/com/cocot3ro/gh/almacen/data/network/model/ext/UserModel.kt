package com.cocot3ro.gh.almacen.data.network.model.ext

import com.cocot3ro.gh.almacen.domain.model.UserDomain
import com.cocot3ro.gh.services.users.UserModel

fun UserModel.toDomain() = UserDomain(
    id = id,
    name = name,
    image = image,
    requiresPassword = requiresPassword,
    role = role.toDomain()
)

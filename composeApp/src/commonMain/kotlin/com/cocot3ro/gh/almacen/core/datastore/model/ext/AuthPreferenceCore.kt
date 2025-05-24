package com.cocot3ro.gh.almacen.core.datastore.model.ext

import com.cocot3ro.gh.almacen.core.datastore.model.AuthPreferenceCore
import com.cocot3ro.gh.almacen.domain.model.AuthPreferenceItem

fun AuthPreferenceCore.toDomain(): AuthPreferenceItem = AuthPreferenceItem(
    jwtToken = jwtToken,
    refreshToken = refreshToken
)
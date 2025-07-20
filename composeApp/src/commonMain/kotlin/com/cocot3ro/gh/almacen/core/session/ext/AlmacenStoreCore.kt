package com.cocot3ro.gh.almacen.core.session.ext

import com.cocot3ro.gh.almacen.core.session.AlmacenStoreCore
import com.cocot3ro.gh.almacen.domain.model.AlmacenStoreDomain

fun AlmacenStoreCore.toDomain(): AlmacenStoreDomain = AlmacenStoreDomain(
    id = storeId,
    name = name
)
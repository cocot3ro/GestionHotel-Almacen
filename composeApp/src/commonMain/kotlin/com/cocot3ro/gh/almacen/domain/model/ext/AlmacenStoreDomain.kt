package com.cocot3ro.gh.almacen.domain.model.ext

import com.cocot3ro.gh.almacen.core.session.AlmacenStoreCore
import com.cocot3ro.gh.almacen.data.network.model.AlmacenStoreModel
import com.cocot3ro.gh.almacen.domain.model.AlmacenStoreDomain

fun AlmacenStoreDomain.toModel(): AlmacenStoreModel = AlmacenStoreModel(
    id = id,
    name = name
)

fun AlmacenStoreDomain.toCore(): AlmacenStoreCore = AlmacenStoreCore(
    storeId = id,
    name = name,
)
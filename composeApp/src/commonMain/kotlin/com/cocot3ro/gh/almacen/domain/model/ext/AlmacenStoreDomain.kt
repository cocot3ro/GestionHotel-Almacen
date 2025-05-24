package com.cocot3ro.gh.almacen.domain.model.ext

import com.cocot3ro.gh.almacen.data.network.model.AlmacenStoreModel
import com.cocot3ro.gh.almacen.domain.model.AlmacenStoreDomain

fun AlmacenStoreDomain.toModel() = AlmacenStoreModel(
    id = id,
    name = name,
    image = image
)
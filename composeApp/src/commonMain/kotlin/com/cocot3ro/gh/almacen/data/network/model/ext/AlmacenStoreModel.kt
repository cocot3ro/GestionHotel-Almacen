package com.cocot3ro.gh.almacen.data.network.model.ext

import com.cocot3ro.gh.almacen.data.network.model.AlmacenStoreModel
import com.cocot3ro.gh.almacen.domain.model.AlmacenStoreDomain

fun AlmacenStoreModel.toDomain() = AlmacenStoreDomain(
    id = id,
    name = name,
    image = image
)
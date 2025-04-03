package com.cocot3ro.gh.almacen.data.database

import com.cocot3ro.gh.almacen.AlmacenEntity
import com.cocot3ro.gh.model.network.data.almacen.AlmacenModel

fun AlmacenEntity.toModel() = AlmacenModel(
    id = id,
    name = name,
    quantity = quantity,
    packSize = packSize,
    minimum = minimum
)
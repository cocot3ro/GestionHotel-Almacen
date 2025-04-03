package com.cocot3ro.gh.almacen.data.database

import com.cocot3ro.gh.almacen.AlmacenEntity
import com.cocot3ro.gh.almacen.data.network.model.AlmacenModel

fun AlmacenEntity.toModel() = AlmacenModel(
    id = id,
    name = name,
    quantity = quantity,
    packSize = packSize,
    minimum = minimum
)
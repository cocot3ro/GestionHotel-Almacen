package com.cocot3ro.gh.almacen.data.network

import com.cocot3ro.gh.almacen.AlmacenEntity
import com.cocot3ro.gh.almacen.data.network.model.AlmacenModel

fun AlmacenModel.toDatabase() = AlmacenEntity(
    id = id,
    name = name,
    quantity = quantity,
    packSize = packSize,
    minimum = minimum
)
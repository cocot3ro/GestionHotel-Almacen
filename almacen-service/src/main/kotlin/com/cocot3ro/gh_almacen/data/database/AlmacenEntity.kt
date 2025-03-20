package com.cocot3ro.gh_almacen.data.database

import com.cocot3ro.gh_almacen.data.network.model.AlmacenModel
import com.cocot3ro.ghalmacen.AlmacenEntity

fun AlmacenEntity.toModel() = AlmacenModel(
    id = id,
    name = name,
    quantity = quantity,
    packSize = packSize,
    minimum = minimum
)
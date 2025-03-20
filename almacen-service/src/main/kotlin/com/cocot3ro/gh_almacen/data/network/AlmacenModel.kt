package com.cocot3ro.gh_almacen.data.network

import com.cocot3ro.gh_almacen.data.network.model.AlmacenModel
import com.cocot3ro.ghalmacen.AlmacenEntity

fun AlmacenModel.toDatabase() = AlmacenEntity(
    id = id,
    name = name,
    quantity = quantity,
    packSize = packSize,
    minimum = minimum
)
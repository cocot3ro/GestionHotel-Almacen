package com.cocot3ro.gh.almacen.data.database.ext

import com.cocot3ro.gh.almacen.AlmacenItemEntity
import com.cocot3ro.gh.almacen.data.network.model.AlmacenItemModel

fun AlmacenItemEntity.toModel() = AlmacenItemModel(
    id = id,
    barcodes = barcodes,
    name = name,
    supplier = supplier,
    image = image,
    quantity = quantity,
    packSize = packSize,
    minimum = minimum
)
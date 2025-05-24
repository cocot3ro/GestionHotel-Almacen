package com.cocot3ro.gh.almacen.data.network.model.ext

import com.cocot3ro.gh.almacen.AlmacenItemEntity
import com.cocot3ro.gh.almacen.data.network.model.AlmacenItemModel

fun AlmacenItemModel.toDatabase() = AlmacenItemEntity(
    id = id,
    barcodes = barcodes,
    name = name,
    supplier = supplier,
    image = image,
    quantity = quantity,
    packSize = packSize,
    minimum = minimum
)